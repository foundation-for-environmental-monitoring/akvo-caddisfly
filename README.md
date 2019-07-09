ffem Water and Soil
===================

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)


ffem Water and ffem Soil apps are simple, low cost, open source, smartphone-based drinking water testing system connected to an online data platform.

See subfolders for more information


Contributing
------------

**Adding illustrations & instructions to tests**

1. Upload the illustrations to [drawable-xhdpi](https://github.com/foundation-for-environmental-monitoring/ffem-app/tree/develop/caddisfly-app/app/src/main/res/drawable-xhdpi) folder. 

    Dimensions: 400 x 400 thereabouts with transparent background.
    Name example: **c_cr_add_reagent_drops.png**
    (Where c = cuvette,  cr = Chromium, and rest a short description of the sentence)

1. Add instruction text by creating a name for each sentence at [strings.xml](https://github.com/foundation-for-environmental-monitoring/ffem-app/blob/develop/caddisfly-app/app/src/main/res/values/strings_cuvette.xml)

    Keep sentences short and highlight important words with bold tags.

```
<string name="c_cr_add_reagent_drops">Add <b>5 drops</b> of Reagent A to the sample.</string>
```

1. Add the above string names and the illustration names (without file extension) to tests json at [tests.json](https://github.com/foundation-for-environmental-monitoring/ffem-app/blob/develop/caddisfly-app/app/src/tryout/assets/tests.json) under the instructions node for each test.

```
"instructions": [
        {
          "section": [
            "c_cr_add_reagent_drops",
            "image:c_cr_add_reagent_drops"
          ]
        }
      ]
```
