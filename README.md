A GUI program allowing to test different server "architectures" in Java

If more than 250 UDP clients - error,

maybe because it gets sent simultaneously and network drops them?

The same for temp TCP - maybe we can't queue so many guys on accept?

---
#### How to launch:
./gradlew localController -> generates build/libs/launcher.jar

./gradlew remoteLauncher  -> generates build/libs/client.jar

Put the launcher on some machine (or just launch on yours)

Launch the client, specifying the ip of the launcher's machine (or leave blank for localhost)

Note: client.jar will create a folder "results" from where it is launched, or continue with the existing one

---

#### How to generate figures:
You need a vanilla R distro and a full directory results/0/ -- 42 files with same params for each architectures

Rscript graphBuilder.r
---
