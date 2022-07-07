# StellarSearcher

Simple and intuitive desktop tool for comprehensive search in catalogue archives and databases.

Supported catalogues and databases:
* VizieR
* MAST
* SIMBAD

## How to build StellarSearcher
1. Clone the project to IDE
2. Compile with **Maven** using ```mvn package```

## How to run StellarSearcher
1. Java 15 or newer is required
2. Start the application from command line ```$ java -jar StellarSearcher.jar```

## How to search in StellarSearcher
1. Manually adding catalogues in VizieR/MAST and specifying input + radius
2. Automatically via input files (below)

### Automated search
1. JSON file following defined structure, example of JSON:
``` 
{
  "targets" : [
    {
      "vizier" : ["I/246", "I/247"],
      "mast" : ["befs"],
      "simbad" : true,
      "input": ["100.1 50.1", "HD 1"],
      "radius" : "10",
      "unit" : "ARCMIN"
    }
  ]
}
```
2. ```vizier```
   - anything you can search in VizieR web service
3. ```mast```
   - befs, copernicus, euve, fuse, hlsp, hpol, hst/hsc, hst/hsc_sum, hst, hut, imaps, iue, k2/data_search, k2/epic, k2/published_planets, kepler/confirmed_planets, kepler/data_search, kepler/kepler_fov, kepler/kgmatch, kepler/kic10, kepler/koi, kepler/published_planets, tues, uit, vlafirst, wuppe, xmm-om
4. ```input```
   - either coordinates (in any format) or identification
5. ```unit```
   - ARCSEC, ARCMIN, DEG
     
