# refreq

A tool for resampling a GTFS

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