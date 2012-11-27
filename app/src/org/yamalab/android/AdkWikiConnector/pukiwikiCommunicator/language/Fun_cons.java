package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;

public class Fun_cons implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_cons(ALisp l)
    {
        lisp=l;
    }
    public LispObject fun(LispObject proc, LispObject argl)
    {
        return lisp.cons(lisp.car(argl),lisp.second(argl));
    }
}

