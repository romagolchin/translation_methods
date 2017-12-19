grammar Grammar;

file: (lexer_rule ';' )* (parser_rule ';')* EOF;
parser_rule: LC_ID  {System.out.println($LC_ID.text);} ARGS? ret? ':' alt ('|' alt)*;
ret: RET ARGS;
alt: elem* CODE?;
lexer_rule: SK? UC_ID ':' STRING;
elem: STRING | (LC_ID '=')? LC_ID {System.out.println($LC_ID.text);}
            ARGS? {}   | UC_ID;

RET: 'returns';
SK: 'skip';
LC_ID: [a-z][a-z0-9]*  ;
UC_ID: [A-Z][A-Z0-9]*;
QUOTE: '\'';
STRING: QUOTE .+? QUOTE;
WS: [ \t\n]+ -> skip;
// FIXME: change to #
//CODE: '#' ((~'#')*? (?=)'"' '"')  '#';
CODE: '{' .*? '}';
//DECL: .+?;
TYPE: .+?;
ARGS: '[' .*? ']';
