package fes.aragon.compilador;
import java_cup.runtime.*;

%%

%class Lexico
%unicode
%cup
%line
%column
%public

%{
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
    public int getYyline(){ return yyline; }
    public int getYy_currentPos(){ return yycolumn; }
%}

DIGITO  = [0-9]
NUMERO  = {DIGITO}+
ID      = [a-zA-Z][a-zA-Z0-9_]*
ESPACIO = [ \t\n\r]+

%%

{ESPACIO}    { /* ignorar */ }

"inicio"     { return symbol(sym.INICIO); }
"mover"      { return symbol(sym.MOVER); }
"arriba"     { return symbol(sym.ARRIBA); }
"abajo"      { return symbol(sym.ABAJO); }
"derecha"    { return symbol(sym.DERECHA); }
"izquierda"  { return symbol(sym.IZQUIERDA); }
"repita"     { return symbol(sym.REPITA); }
"hasta"      { return symbol(sym.HASTA); }
"fin"        { return symbol(sym.FIN); }
"="          { return symbol(sym.ASIG); }

{NUMERO}     { return symbol(sym.NUMERO, Integer.parseInt(yytext())); }
{ID}         { return symbol(sym.ID, yytext()); }

[^]          { System.out.println("Error lexico: " + yytext() + " linea " + yyline); }