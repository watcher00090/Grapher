public enum Op { 
    Plus, Minus, Times, Divide, Hat, Undef;

    public String toString() {
        switch(this) {
        case Plus: return "+"; 
        case Minus: return "-";
        case Times: return "*";
        case Divide: return "/";
        case Hat: return "^";
        default: return "INVALID_OPERATOR";
        }
    }

    public static boolean isOp(char c) { 
        if (c == '+') return true;
        if (c == '-') return true;
        if (c == '*') return true;
        if (c == '/') return true;
        if (c == '^') return true;
        return false;
    }

    public static Tok getOp(char c) { 
        if (c == '+') return Tok.PLUS;
        if (c == '-') return Tok.MINUS;
        if (c == '*') return Tok.TIMES;
        if (c == '/') return Tok.DIVIDE;
        if (c == '^') return Tok.HAT;
        return Tok.UNDEF;
    }

}

