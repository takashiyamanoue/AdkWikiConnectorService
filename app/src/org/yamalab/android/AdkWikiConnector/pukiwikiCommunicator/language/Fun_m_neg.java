package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;

public class Fun_m_neg implements PrimitiveFunction
{
    public ALisp lisp;
    public Fun_m_neg(ALisp l)
    {
        lisp=l;
    }
    public LispObject fun(LispObject proc, LispObject argl)
    {
                MyNumber x=(MyNumber)(lisp.car(argl));
                return x.neg();
    }
}

