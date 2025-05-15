**How to run the program**

You need the Java Version 11 (JDK) installed - check with java --version (you can download the necessary jdk from https://www.openlogic.com/openjdk-downloads )

First time steps:
1.  Go to your favourite folder and run the following command: git clone https://github.com/biodivAtlasAT/duk-ala-data-convert.git -b master
2.  cd duk-ala-data-convert
3.  WINDOWS: .\gradlew.bat UNIX: ./gradlew
4.  Edit and configure config.properties (the species-lists must be the same as in the survey configuration in biocollect; multiple lists must be separated by comma); the template must correspond to the template of the survey, which can be downloaded from biocollect   


    template=<filepath to the xlsx-template used by the survey; e.g. Single_Sighting_Universal.xlsx>  
    speciesLists=<name of the lists from list-tool which are used by the survey; e.g.: dr52,dr17>  
    listsUrl=<URL to the list tool; e.g.: https://lists.biodivdev.at/ws/speciesListItems >  
    bieUrl=<URL to the bie tool, to check the scienitific  names; e.g. https://bie.biodivdev.at/ws/guid >  
    collectoryUrl=<URL to the collectory, to check various codes; e.g. https://collectory.biodivdev.at/ws >  
  
5.  Determine the start parameters: see start script below as an example; the institution and collection codes must be defined in collectory

Run the program (the first time it needs some time to compile), to run the example exchange "PATH_TO_XLSX_FILE" with: example\\naturbeobachtung_test.xlsx  
6. WINDOWS:  .\gradlew run --args="--cfg-file=example.config.properties --ifile "PATH_TO_XLSX_FILE" --imodel=NATURSCHUTZBUND --count=60 --inst-code=GLOBAL2K --coll-code=HERPF"  


    Options:  
    -v, --verbose                 Show Details  
    --ifile=<text>                Name of the input file to convert  
    --imodel=<(naturschutzbund)>  Name of the input file model - implemented interfaces for different data providers  
    --count=<int>                 Count of rows to transform (if you want to import the first n rows)  
    --inst-code=<text>            Providermap for institution (must exist in collectory)  
    --coll-code=<text>            Providermap for collection (must exist in collectory)  
    --cfg-file=<text>             Name of the configuration file (e.g. config.properties)  
    -h, --help                    Show this message and exit  
  
7. The resulting (converted) xlsx-file is created in the same directory as the --ifile but with the prefix "Conv"
8.  Check the app.log im the logs directory - change or adapt erroneous rows (discuss with the data provider) and repeat
9.  The converted file can be uploaded into the Biocollect-Tool (bulk-upload of survey - built with the template "Single Sighting Universal")
