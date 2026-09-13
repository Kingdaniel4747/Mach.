# Daten & Berechtigungen

MACH ist ohne Benutzerkonto und ohne eigenen Cloud-Dienst konzipiert. Die lokale Oberfläche enthält keine Werbe- oder Analyse-SDKs. Beim App-Start fragt MACH ausschließlich die öffentliche GitHub-Release-API nach einer neueren Version ab. Erst nach „Jetzt aktualisieren“ wird die veröffentlichte APK über Androids Download-Manager geladen; die Installation bestätigt der Nutzer im Android-Systemdialog.

## Was wird gespeichert?

Einstellungen, Wecker, Aufgaben, Habits, Morgenchecks, importierte Alarmtöne, ausgewählte App-Pakete, NFC-/QR-Schlüssel und Sperrregeln bleiben in den lokalen App-Daten. Die Oberfläche nutzt lokalen WebView-Speicher; native Funktionen nutzen app-eigene Dateien und Android SharedPreferences.

Die App ist kein verschlüsselter Datentresor. Ein Gerätezugriff, Root-Zugriff oder Zugriff auf Backups kann Daten offenlegen.

## Was sieht der App-Blocker?

Die Bedienungshilfe verarbeitet Fenster-/App-Ereignisse, um die aktive App zu erkennen und ausgewählte Apps zu sperren. Sie ist nicht zum Auslesen von Bildschirmtext eingerichtet (`canRetrieveWindowContent=false`).

Der Nutzungsdatenzugriff liefert Android-Ereignisse zur App-Nutzung. MACH berechnet daraus die heutige Vordergrundzeit. Es greift nicht auf private Samsung-Datenbanken zu.

## NFC, QR und Passwort

NFC dient zum Wiedererkennen des angelernten Tags. Der QR-Scanner prüft den erwarteten lokalen Code. Blocker-Passwörter werden als Prüfsumme gespeichert. Diese Mechanismen dienen der Selbstorganisation, nicht als manipulationssichere Gerätesperre oder Schutz vor einem Angreifer mit Gerätezugriff.

Keine NFC-Kennungen, QR-Inhalte, Passwörter oder personenbezogenen Logs in öffentlichen Issues teilen.

## Android-Backup

Die automatische Android-Cloud-Sicherung und die Übernahme der App-Daten auf ein anderes Gerät sind deaktiviert. MACH schließt dafür auf älteren und neueren Android-Versionen sämtliche internen Speicherbereiche aus. Ein Backup entsteht nur, wenn es in MACH bewusst als Datei exportiert wird.

## Löschen

Deinstallation oder „App-Daten löschen“ entfernt die lokalen App-Daten. Selbst exportierte MACH-Backup-Dateien bleiben als eigene Dateien erhalten und können bei Bedarf manuell gelöscht werden. Vor einem normalen Update muss die App nicht deinstalliert werden.

[Zurück zur README](../README.md)
