//Token Types. UNDEF = pushback token when nothing's been pushed back, token returned when state is unknown.
public enum Tok { 
    LPAR, RPAR, PLUS, MINUS, TIMES, DIVIDE, HAT, NUMBER, VARIABLE, 
    FUNCTION, SUMMATION, PRODUCT, REX, PARAM, 
    EQUALS, COMMA, COLON, EOS, UNDEF, INVALID, 
    E, C, G, g, PI;
}
