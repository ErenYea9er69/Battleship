# Ensure bin directory exists
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin"
}

# Find all Java source files
$srcFiles = Get-ChildItem -Path "connectfour" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

Write-Host "Compiling Java files..." -ForegroundColor Cyan
# Compile using JavaFX modules
javac --module-path "javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -d bin $srcFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copying FXML resources..." -ForegroundColor Cyan
    if (-not (Test-Path "bin\com\connectfour")) {
        New-Item -ItemType Directory -Path "bin\com\connectfour" -Force
    }
    Copy-Item -Path "connectfour\*.fxml" -Destination "bin\com\connectfour\" -Force

    
    Write-Host "Compilation successful. Launching Connect Four..." -ForegroundColor Green
    # Run application
    java --module-path "javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp bin com.connectfour.Main
} else {
    Write-Host "Compilation failed." -ForegroundColor Red
}
