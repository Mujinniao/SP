@echo off

call "%~dp0\gradlew" assembleRelease --no-daemon

call "D:\CatVodSpider-main\jar\3rd\1.bat" %1

pause