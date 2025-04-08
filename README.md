**How to run the program**
1.  git clone https://github.com/biodivAtlasAT/duk-ala-data-convert.git -b master
2.  cd duk-ala-data-convert
3.  WINDOWS: .\gradlew.bat UNIX: ./gradlew
4.  Edit and configure config.properties (the lists must be the same as in the survey configuration in biocollect; multiple lists must be separated by comma; the template must correspond to the template of the survey, which can be downloaded from biocollect
5.  Determine the start parameters: see start script below as an example
6.  WINDOWS:  .\gradlew run --args="--cfg-file=config.properties --ifile "PATH_TO_XLSX_FILE" --imodel=NATURSCHUTZBUND --count=200000 --inst-code=GLOBAL2K --coll-code=HERPF"
7.  The resulting (converted) xlsx-file is created in the same directory as the --file but with the prefix "Conv"
8.  Check the app.log im the logs directory - change or adapt erroneous rows (discuss with the data provider) and repeat
9.  The converted file can be uploaded into the Biocollect-Tool (bulk-upload of survey - built with the template "Single Sighting Universal")
