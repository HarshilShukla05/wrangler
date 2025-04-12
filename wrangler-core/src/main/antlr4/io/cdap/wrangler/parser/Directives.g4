grammar Directives;

options {
  language = Java;
}

@lexer::header {
  /*
   * Copyright © 2017-2019 Cask Data, Inc.
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   * http://www.apache.org/licenses/LICENSE-2.0
   */
}

recipe
 : statements EOF
 ;

statements
 :  ( Comment | macro | directive SColon | pragma SColon | ifStatement )*
 ;

directive
 : command
   ( codeblock
   | identifier
   | macro
   | text
   | number
   | bool
   | column
   | colList
   | numberList
   | boolList
   | stringList
   | numberRanges
   | properties
   )*?
 ;

ifStatement
 : ifStat elseIfStat* elseStat? CBrace
 ;

ifStat
 : If expression OBrace statements
 ;

elseIfStat
 : CBrace Else If expression OBrace statements
 ;

elseStat
 : CBrace Else OBrace statements
 ;

expression
 : OParen (~OParen | expression)* CParen
 ;

forStatement
 : For OParen Identifier Assign expression SColon expression SColon expression CParen OBrace statements CBrace
 ;

macro
 : Dollar OBrace (~OBrace | macro | Macro)*? CBrace
 ;

pragma
 : Hash Pragma (pragmaLoadDirective | pragmaVersion)
 ;

pragmaLoadDirective
 : LoadDirectives identifierList
 ;

pragmaVersion
 : Version Number
 ;

codeblock
 : Exp Space* Colon condition
 ;

identifier
 : Identifier
 ;

properties
 : Prop Colon OBrace (propertyList)+  CBrace
 | Prop Colon OBrace OBrace (propertyList)+ CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | Prop Colon OBrace (propertyList)+ CBrace CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | Prop Colon (propertyList)+ CBrace { notifyErrorListeners("Missing opening brace"); }
 | Prop Colon OBrace (propertyList)+  { notifyErrorListeners("Missing closing brace"); }
 ;

propertyList
 : property (Comma property)*
 ;

property
 : Identifier Assign ( text | number | bool )
 ;

numberRanges
 : numberRange (Comma numberRange)*
 ;

numberRange
 : Number Colon Number Assign value
 ;

value
 : String | Number | Column | Bool | BYTE_SIZE | TIME_DURATION
 ;

ecommand
 : External Identifier
 ;

config
 : Identifier
 ;

column
 : Column
 ;

text
 : String
 ;

number
 : Number
 ;

bool
 : Bool
 ;

condition
 : OBrace (~CBrace | condition)* CBrace
 ;

command
 : Identifier
 ;

colList
 : Column (Comma Column)+
 ;

numberList
 : Number (Comma Number)+
 ;

boolList
 : Bool (Comma Bool)+
 ;

stringList
 : String (Comma String)+
 ;

identifierList
 : Identifier (Comma Identifier)*
 ;


/*
 * Lexer Rules
 */
OBrace   : '{';
CBrace   : '}';
SColon   : ';';
Or       : '||';
And      : '&&';
Equals   : '==';
NEquals  : '!=';
GTEquals : '>=';
LTEquals : '<=';
Match    : '=~';
NotMatch : '!~';
QuestionColon : '?:';
StartsWith : '=^';
NotStartsWith : '!^';
EndsWith : '=$';
NotEndsWith : '!$';
PlusEqual : '+=';
SubEqual : '-=';
MulEqual : '*=';
DivEqual : '/=';
PerEqual : '%=';
AndEqual : '&=';
OrEqual  : '|=';
XOREqual : '^=';
Pow      : '^';
External : '!';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';
Modulus  : '%';
OBracket : '[';
CBracket : ']';
OParen   : '(';
CParen   : ')';
Assign   : '=';
Comma    : ',';
QMark    : '?';
Colon    : ':';
Dot      : '.';
At       : '@';
Pipe     : '|';
BackSlash: '\\';
Dollar   : '$';
Tilde    : '~';
Hash     : '#';

If    : 'if';
Else  : 'else';
For   : 'for';
Exp   : 'exp';
Prop  : 'prop';
Version : 'version';
LoadDirectives : 'load-directives';
Pragma : 'pragma';

Bool
 : 'true'
 | 'false'
 ;

Number
 : Int (Dot Digit*)?
 ;

BYTE_SIZE: Digit+ (Dot Digit+)? BYTE_UNIT;
TIME_DURATION: Digit+ (Dot Digit+)? TIME_UNIT;

fragment BYTE_UNIT: [KkMmGgTt][Bb];
fragment TIME_UNIT: ('ms' | 's' | 'm' | 'h');

Identifier
 : [a-zA-Z_\-] [a-zA-Z_0-9\-]*
 ;

Macro
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

Column
 : ':' [a-zA-Z_\-] [:a-zA-Z_0-9\-]*
 ;

String
 : '\'' ( EscapeSequence | ~('\''))* '\''
 | '"'  ( EscapeSequence | ~('"'))* '"'
 ;

EscapeSequence
   :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
   |   UnicodeEscape
   |   OctalEscape
   ;

fragment
OctalEscape
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment
UnicodeEscape
   :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
   ;

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;

fragment Int
 : '-'? [1-9] Digit* [L]*
 | '0'
 ;

fragment Digit
 : [0-9]
 ;
