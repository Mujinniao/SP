@echo off

copy "D:\CatVodSpider-main\app\build\intermediates\dex\release\minifyReleaseWithR8\classes.dex" "D:\CatVodSpider-main\jar"

del "custom_spider.jar"
rd /s/q "out"

java -jar "3rd\baksmali-2.5.2.jar" d "classes.dex"

rd /s/q "spider.jar\smali\com\github\catvod\spider"
rd /s/q "spider.jar\smali\com\github\catvod\parser"
rd /s/q "spider.jar\smali\com\github\catvod\js"

if not exist "spider.jar\smali\com\github\catvod\" md "spider.jar\smali\com\github\catvod\"


 java -Dfile.encoding=utf-8 -jar "oss.jar" "out"


move "out\com\github\catvod\spider" "spider.jar\smali\com\github\catvod\"
move "out\com\github\catvod\parser" "spider.jar\smali\com\github\catvod\"
move "out\com\github\catvod\js" "spider.jar\smali\com\github\catvod\"


java -jar "3rd\apktool_2.4.1.jar" b "spider.jar" -c

move "spider.jar\dist\dex.jar" "custom_spider.jar"

certUtil -hashfile "custom_spider.jar" MD5 | find /i /v "md5" | find /i /v "certutil" > "custom_spider.jar.md5"

rd /s/q "spider.jar\smali
rd /s/q "spider.jar\build"
rd /s/q "spider.jar\dist"
del "classes.dex"
