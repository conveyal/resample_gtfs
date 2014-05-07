# refreq

A tool for resampling a GTFS

## prereqs

- Gradle

## build

    $ gradle fatJar
    
## running

    $ java -jar ./build/libs/resample_gtfs.jar gtfs_feed_path resample_file
    
With parameters:
- gtfs_feed_path: A path to a gtfs feed.
- resample_file: A path to the JSON resample file
