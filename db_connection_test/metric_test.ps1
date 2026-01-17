function Get-CpuUsage ($START_TS, $END_TS) {
  $NUM_CORES = 2
  
  $CPU_METRIC = "container_cpu_usage_seconds_total{name='desktop-postgres-1'}"
  $CPU_QUERY = "$CPU_METRIC @ $END_TS - $CPU_METRIC @ $START_TS"
  $URI = "http://localhost:9090/api/v1/query?query=$([uri]::EscapeDataString($CPU_QUERY))"
  $CPU_RESPONSE = Invoke-RestMethod -Uri $URI

  $DURATION = $END_TS - $START_TS
  
  $cpuSeconds = [double]$CPU_RESPONSE.data.result[0].value[1]
  $percentage = $cpuSeconds * 100 / ($DURATION * $NUM_CORES)
  
  return $percentage
}

function Get-MemoryUsage ($END_TS) {
  $METRIC = "container_memory_usage_bytes{name='desktop-postgres-1'}"
  $QUERY = "$METRIC @ $END_TS"
  $URI = "http://localhost:9090/api/v1/query?query=$([uri]::EscapeDataString($QUERY))"
  $RESPONSE = Invoke-RestMethod -Uri $URI
  
  return [double]$RESPONSE.data.result[0].value[1]/(1024*1024)
}

function Get-WrittenMegaBytes ($START_TS, $END_TS) {
  $METRIC = "container_fs_writes_bytes_total{name='desktop-postgres-1'}"
  $QUERY = "$METRIC @ $END_TS - $METRIC @ $START_TS"
  $URI = "http://localhost:9090/api/v1/query?query=$([uri]::EscapeDataString($QUERY))"
  $RESPONSE = Invoke-RestMethod -Uri $URI
  
  return [double]$RESPONSE.data.result[0].value[1]/(1024*1024)
}

$START_TS = [DateTimeOffset]::Now.ToUnixTimeSeconds()

k6 run test.js

$END_TS = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$END_TS_PLUS_THREE = $END_TS + 3

Start-Sleep -Seconds 5

$cpuUsage = Get-CpuUsage $START_TS $END_TS_PLUS_THREE
$memoryUsage = Get-MemoryUsage $END_TS_PLUS_THREE
$writtenMegaBytes = Get-WrittenMegaBytes $START_TS $END_TS_PLUS_THREE
Write-Output "CPU Usage: $cpuUsage %"
Write-Output "Memory Usage: $memoryUsage MB"
Write-Output "Written Bytes: $writtenMegaBytes MB"