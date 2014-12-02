# resample_gtfs

[resample_gtfs](https://github.com/conveyal/resample_gtfs) is a tool used to change the level of service in an existing GTFS feed. It performs a similar task to [refreq](https://github.com/conveyal/refreq), but creates bona fide trips, while refreq produces frequencies. This is accomplished by using a configuration file to identify a set of existing GTFS trips, and a target level of service to replace those trips. For example: we may specify that we want to resample all trips for route 255 between 6am and 9am to run every half hour, whereas before it may have operated more frequently. We do this by composing a configuration file that looks like this:

    {
      "windows": [
        {
          "start": 6,
          "end": 9,
          "name": "peak_am",
          "service_ids": [
            "WEEKDAY"
          ]
        },
      ],
      "routes": [
        {
          "route": "255",
          "headways": {
            "peak_am": 30.0,
          },
        },
      ]
    }

And then run the resample_gtfs tool:

    $ resample_gtfs original_gtfs.zip config.json resampled_gtfs.json

This will go through the original_gtfs.zip file, and look for all trips running on service_id “WEEKDAY” associated with a route with the short name “255”. It groups them by direction, and then picks an exemplar route for each direction. Then it copies that trip at 30 minute intervals between 6am and 9am, setting the service_id for the trip to “WEEKDAY” but otherwise leaving the rest of the GTFS alone.

The config.json file for resampling King County is somewhat more complicated. I wrote a small script for converting the service cut spreadsheet to a config.json file. Here are some example “routes” entries:

    {
          "route": "1",
          "headways": {
            "peak_am": 15.0,
            "sun": null,
            "peak_pm": 15.0,
            "night": 45.0,
            "midday": 30.0,
            "sat": null
          },
          "trips": {
            "peak_am": null,
            "peak_pm": null
          }
    }

Note there are two ways of specifying how resample_gtfs fills out a service window: ‘headways’ or ‘trips’. If there’s an entry in the ‘headways’ section, resample_gtfs will fill up the service window with the headway. Using ‘trips’, resample_gtfs will place the specified number of trips in the middle of the service window with a headway deduced from the original GTFS.

    {
          "route": "9EX",
          "trip_filters":[["trip_short_name","EXPRESS"]],
          "headways": {
            "peak_am": null,
            "sun": null,
            "peak_pm": null,
            "night": null,
            "midday": null,
            "sat": null
          },
          "trips": {
            "peak_am": 9,
            "peak_pm": 8
          }
    }

This routes entry has a property “trip_filters”, which specifies that resample_gtfs should only resample trips of route “9” that have the ‘trip_short_name’ of “EXPRESS”. This will leave all non-express trips unaffected.

Finally, there are routes entry like:

    {
          "suppress": true,
          "route": "2",
    }

All trips for the route 2 will simply be left out of the resampled GTFS. Remember, we created a fake GTFS out of a shapefile with geom2gtfs. We need some way to keep all those routes out of the resampled GTFS, so we when we piece them together as an OpenTripPlanner graph, we don’t end up with two colliding routes wherever we produced a route in geom2gtfs.

## prereqs

- Gradle

## build

    $ gradle fatJar

## running

    $ java -jar ./build/libs/resample_gtfs.jar input_gtfs service_file output_gtfs

With parameters:
- input_gtfs: A path to a gtfs feed.
- resample_file: A path to the JSON resample file. For an example look at data/service.json.example
- output_gtfs: A path to a gtfs feed to be created.
