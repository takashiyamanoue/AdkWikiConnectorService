package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;

public class Fun_car implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_car(ALisp l)
    {
        lisp=l;
    }
    public LispObject fun(LispObject proc, LispObject argl)
    {
        return lisp.car(lisp.car(argl));
    }
}

