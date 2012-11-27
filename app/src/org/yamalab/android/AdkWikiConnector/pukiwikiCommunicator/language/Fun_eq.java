package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;

public class Fun_eq implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_eq(ALisp l)
    {
        lisp=l;
    }
    public LispObject fun(LispObject proc, LispObject argl)
    {
        return lisp.eq2(lisp.car(argl),lisp.second(argl));
    }
}

