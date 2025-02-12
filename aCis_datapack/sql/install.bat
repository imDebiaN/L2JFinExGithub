@echo off

REM ############################################
REM ## You can change here your own DB params ##
REM ############################################
REM MYSQL BIN PATH
set mysqlBinPath=C:\Program Files\MySQL\MySQL Server 5.7\bin

REM LOGINSERVER
set lsuser=
set lspass=
set lsdb=finex_login_database
set lshost=localhost

REM GAMESERVER
set gsuser=
set gspass=
set gsdb=finex_game_database
set gshost=localhost
REM ############################################

set mysqldumpPath="%mysqlBinPath%\mysqldump"
set mysqlPath="%mysqlBinPath%\mysql"

echo.-------------------------------
echo.Installation of fin-ex database
echo.-------------------------------
echo.

echo.Login database.
echo.
for %%i in (login/*.sql) do (
echo install %%i
%mysqlPath% -h %lshost% -u %lsuser% --password=%lspass% -D %lsdb% < login/%%i
)

echo.
echo.Game database.
echo.
for %%i in (game/*.sql) do (
echo install %%i
%mysqlPath% -h %gshost% -u %gsuser% --password=%gspass% -D %gsdb% < game/%%i
)

echo.
echo Installation complete.
echo.
pause
