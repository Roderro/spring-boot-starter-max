$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

$scenarios = @(
    @{
        Name = "minimal"
        Starter = Join-Path $repoRoot "examples\src\main\java\ru\maxbot\examples\minimal"
        RawApi = Join-Path $repoRoot "vkr\experiment\raw-api\minimal"
    },
    @{
        Name = "security"
        Starter = Join-Path $repoRoot "examples\src\main\java\ru\maxbot\examples\security"
        RawApi = Join-Path $repoRoot "vkr\experiment\raw-api\security"
    },
    @{
        Name = "fsm"
        Starter = Join-Path $repoRoot "examples\src\main\java\ru\maxbot\examples\fsm"
        RawApi = Join-Path $repoRoot "vkr\experiment\raw-api\fsm"
    }
)

function Measure-JavaDirectory {
    param([string]$Path)

    $files = Get-ChildItem -LiteralPath $Path -Recurse -File -Filter *.java
    $lineCount = 0

    foreach ($file in $files) {
        foreach ($line in Get-Content -LiteralPath $file.FullName) {
            if (-not [string]::IsNullOrWhiteSpace($line)) {
                $lineCount++
            }
        }
    }

    [pscustomobject]@{
        Files = $files.Count
        Lines = $lineCount
    }
}

$rows = foreach ($scenario in $scenarios) {
    $starter = Measure-JavaDirectory -Path $scenario.Starter
    $rawApi = Measure-JavaDirectory -Path $scenario.RawApi
    $reduction = if ($rawApi.Lines -eq 0) { 0 } else { [math]::Round((1 - ($starter.Lines / $rawApi.Lines)) * 100, 1) }

    [pscustomobject]@{
        Scenario = $scenario.Name
        StarterFiles = $starter.Files
        StarterLines = $starter.Lines
        RawApiFiles = $rawApi.Files
        RawApiLines = $rawApi.Lines
        ReductionPercent = $reduction
    }
}

$rows | Format-Table -AutoSize
