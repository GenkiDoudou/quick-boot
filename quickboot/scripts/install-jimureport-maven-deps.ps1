# 将积木报表所需、仅存在于 Jeecg 私服的 JAR 安装到本地 ~/.m2（需联网，仅首次）
$ErrorActionPreference = "Stop"
$base = "https://maven.jeecg.org/nexus/content/repositories/jeecg"
$tmp = Join-Path $env:TEMP "jimureport-maven-deps"
New-Item -ItemType Directory -Force -Path $tmp | Out-Null

function Install-Jar($relPath, $groupId, $artifactId, $version) {
    $url = "$base/$($relPath -replace '\\','/')"
    $jar = Join-Path $tmp "$artifactId-$version.jar"
    Write-Host "Downloading $url ..."
    curl.exe -fsSL -o $jar $url
    mvn install:install-file `
        "-Dfile=$jar" `
        "-DgroupId=$groupId" `
        "-DartifactId=$artifactId" `
        "-Dversion=$version" `
        "-Dpackaging=jar" `
        "-DgeneratePom=true"
}

Install-Jar "gui/ava/html2image/2.0.1/html2image-2.0.1.jar" "gui.ava" "html2image" "2.0.1"
Install-Jar "com/github/promeg/tinypinyin/2.0.3/tinypinyin-2.0.3.jar" "com.github.promeg" "tinypinyin" "2.0.3"

Write-Host "Done. Re-run: mvn -pl quickboot-report,quickboot-web -am compile"
