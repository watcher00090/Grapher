enum State { 
    INIT, WORD, INTEGER, DOUBLE, DONE;
}

public class Tokenizer { 

    String in;
    char[] chars; 
    int i;
    double numVal; //start as an int, changes to double if a period is found.
    String strVal; //stores function or variable names
    State state;        
    Tok backToken;

    Tokenizer(String in) { 
        this.in  = in;
        numVal = 0.0;
        strVal = "";
        i = 0;
        state = State.INIT;
        backToken = Tok.UNDEF;
        chars = new char[in.length()+1];
        for (int k=0; k<in.length(); k++){
            chars[k]=in.charAt(k);    
        }
        chars[in.length()] = 0; //end with the null byte character, so the Tokenizer knows when the String is done.
    }

    public Tok nextToken() { 
        Tok t = nextTokenContinued();
        //System.out.println(t);
        return t;
    }

    /*
     * Fewer States is desirable. Operators and punctuation are single
     * characters, so there isn't any reason to define separate states for each
     * one. Therefore, when such a character is encountered, the State switches
     * to State.INIT, and a token indicating corresponding to that character is
     * returned.
     * 
     * Every time a character is found that changes the state, i(the pointer
     * that stores where the tokenizer is in the String) is decremented so that
     * the next call to nextToken() so that the character that changed the
     * state isn't skipped over.
     * 
     * When the end of the String is reached, repeated calls to nextToken()
     * return Tok.EOState.
     * 
     * @return the next token in the input String.
     */
    public Tok nextTokenContinued() { 
        if (backToken !=Tok.UNDEF) {    
            Tok result = backToken;      
            backToken = Tok.UNDEF;    
            return result;             
        }

        numVal = 0.0;
        strVal = "";    
        int p = 0;    // stores double decimal point digits.
        state = State.INIT; // State is internal. So when nextToken is called,
                            // the code shouldn't know what state it's in. 

        while (true) { 

            char c = chars[i++]; 

            switch (state) { 
            case INIT: { //Initial scanner state 
                if (Character.isAlphabetic(c)) { 
                    state = State.WORD;
                    strVal+=c;    
                }
                else if (Character.isDigit(c)) { 
                    state = State.INTEGER;
                    numVal = 10*numVal + (c - '0') ; //add the digit to the number
                }
                else if (Op.isOp(c)) return Op.getOp(c);
                else if (c == '(') {
                    return Tok.LPAR;
                }
                else if (c == ')') return Tok.RPAR;
                else if (c == '.') state = State.DOUBLE;
                else if (c == ',') return Tok.COMMA;
                else if (c == ':') return Tok.COLON;
                else if (c == '!') return Tok.FUNCTION;
                else if (c == '=') return Tok.EQUALS;
                else if (c == 0) { //DONE must know that we've reached the end.
                    i--;            //allow repeated calls to return Tok.EOS
                    state = State.DONE;    
                }
                else if (Character.isWhitespace(c)) continue; // ignore whitespace between
                                                              // operators or punctuation 
                else return Tok.INVALID;    
                break;
            }
            case INTEGER: { 
                if (Character.isAlphabetic(c)) { 
                    return Tok.INVALID;
                }
                else if (Character.isDigit(c)) { 
                    numVal = 10*numVal + (c -'0');    //add the digit to the number
                }
                else if (Op.isOp(c) || c =='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) {
                    if (c==0) state = State.DONE;
                    i--; //the next call to nextToken() needs to know the character.
                    return Tok.NUMBER;
                }
                else if (c == '.') {
                    state = State.DOUBLE;
                }
                else {
                    return Tok.INVALID;
                }
                break;
            }
            case DOUBLE: {
                if (Character.isAlphabetic(c)) { 
                    return Tok.INVALID;
                }
                else if (Character.isDigit(c)){ 
                    p++;
                    numVal = numVal + (c -'0')/(Math.pow(10, p));
                }            
                else if (c == '(' || c ==')' || c==',' || Character.isWhitespace(c) || Op.isOp(c) || c==0) {
                    if (c==0) state = State.DONE;
                    i--; 
                    return Tok.NUMBER;
                }
                else { 
                    return Tok.INVALID;
                }
                break;
            }
            case WORD: {
                if (Character.isAlphabetic(c) || Character.isDigit(c)) { 
                    strVal+=c;
                }
                else if (Op.isOp(c) || c=='(' || c==')' || c==',' || Character.isWhitespace(c) || c==0) { 
                    i--;
                    if (c==0) state = State.DONE;
                    if (Func.isFunc(strVal)) return Tok.FUNCTION;
                    if (strVal.equals( "sum" )) return Tok.SUMMATION;
                    if (strVal.equals( "prod" )) return Tok.PRODUCT;
                    if (strVal.equals( "rex" )) return Tok.REX;
                    if (strVal.equals( "param" )) return Tok.PARAM;
                    if (strVal.equals( "e" )) return Tok.E;
                    if (strVal.equals( "pi")) return Tok.PI;
                    if (strVal.equals( "c")) return Tok.C;
                    if (strVal.equals( "G")) return Tok.G;
                    if (strVal.equals( "g")) return Tok.g;
                    return Tok.VARIABLE;
                }
                else {
                    return Tok.INVALID;
                }
                break;
            }
            case DONE: return Tok.EOS; 
            default: return Tok.UNDEF; //undefined state error.
            }
        }    
    }

}
