param(
    [Parameter(Mandatory=$true)][string]$UnsignedApk,
    [Parameter(Mandatory=$true)][string]$ApkSignerJar,
    [string]$Java = 'D:/MATLAB/sys/java/jre/win64/jre/bin/java.exe',
    [string]$OutputApk = ''
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$keyPath = Join-Path $repoRoot '.local/xinqing-signing.p12'
if (-not (Test-Path -LiteralPath $keyPath)) {
    throw '固定签名文件缺失。请恢复本机密钥备份，不要生成新密钥，否则无法覆盖升级。'
}
if (-not $OutputApk) { $OutputApk = Join-Path $repoRoot '.local/xinqing-signed.apk' }
& $Java -jar $ApkSignerJar sign --ks $keyPath --ks-type PKCS12 --ks-key-alias xinqing --ks-pass pass:android --key-pass pass:android --out $OutputApk $UnsignedApk
if ($LASTEXITCODE -ne 0) { throw 'APK 签名失败' }
& $Java -jar $ApkSignerJar verify --verbose --print-certs $OutputApk
if ($LASTEXITCODE -ne 0) { throw 'APK 签名验证失败' }
Write-Output "已签名并验证：$OutputApk"
