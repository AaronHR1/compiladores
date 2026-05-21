@echo off
echo ================================
echo  Generando Lexico.java (JFlex)
echo ================================
java -jar libs/JFlex.jar flex/Lexico.flex

echo.
echo ================================
echo  Generando parser.java (CUP)
echo ================================
java -jar libs/java-cup-11.jar -parser parser -symbols sym cup/Parser.cup

echo.
echo ================================
echo  Moviendo archivos generados...
echo ================================

IF EXIST Lexico.java (
    move Lexico.java src\main\java\fes\aragon\compilador\
) ELSE IF EXIST flex\Lexico.java (
    move flex\Lexico.java src\main\java\fes\aragon\compilador\
) ELSE (
    echo ERROR: No se encontro Lexico.java
)

IF EXIST parser.java (
    move parser.java src\main\java\fes\aragon\compilador\
)

IF EXIST sym.java (
    move sym.java src\main\java\fes\aragon\compilador\
)

echo.
echo Listo!
pause