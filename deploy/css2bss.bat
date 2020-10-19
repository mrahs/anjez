for %%f in (G:\dev\anjez\app\src\pw\ahs\app\anjez\gui\css\*.bss) do (
    IF EXIST "%%f" del "%%f"
)

"c:\Program Files\Java\jdk1.8.0\bin\javafxpackager.exe" -createbss -srcdir "G:\dev\anjez\app\res\" -outdir "G:\dev\anjez\app\src\pw\ahs\app\anjez\gui\css"
