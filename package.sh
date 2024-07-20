
# /!\ while making the jar file, don't forget to add javafx lib content ( for both oses )
# and create directory com/qsync/qagenda where we can put ressource files like templates (.fxml)

jpackage --input out/artifacts/QAgenda_jar --name QAgenda --main-jar /home/thaao/IdeaProjects/QAgenda/out/artifacts/QAgenda_jar/QAgenda.jar --main-class com.qsync.qagenda.Main_1 --icon agenda.png --type exe --win-menu --win-shortcut
jpackage --input out/artifacts/QAgenda_jar --name QAgenda --main-jar /home/thaao/IdeaProjects/QAgenda/out/artifacts/QAgenda_jar/QAgenda.jar --main-class com.qsync.qagenda.Main_1 --icon agenda.png --type deb --linux-shortcut