package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language;

public class MyInt extends MyNumber
{
	public static String kind="myint";
	public boolean isKind(String x){
		return x.equals(kind);
	}
    public MyNumber neg()
    {
        return new MyInt(-val);
    }
    public boolean ne(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val!=((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val!=((MyDouble)y).val;
        }
        return false;
    }
    public MyNumber mod(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return new MyInt(val%((MyInt)y).val);
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return new MyDouble(val%((MyDouble)y).val);
        }
        return null;
    }
    public MyNumber tan()
    {
        return new MyDouble(Math.tan((double)val));
    }
    public MyNumber sqrt()
    {
        return new MyDouble(Math.sqrt((double)val));
    }
    public MyNumber sin()
    {
        return new MyDouble(Math.sin((double)val));
    }
    public MyNumber log()
    {
        return new MyDouble(Math.log((double)val));
    }
    public MyNumber exp()
    {
        return new MyDouble(Math.exp((double)val));
    }
    public MyNumber cos()
    {
        return new MyDouble(Math.cos((double)val));
    }
    public MyNumber atan()
    {
        return new MyDouble(Math.atan((double)val));
    }
    public int getInt()
    {
        return val;
    }
    public boolean ge(MyNumber y)
    {
//         if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val>=((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val>=((MyDouble)y).val;
        }
        return false;
   }
    public boolean le(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val<=((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val<=((MyDouble)y).val;
        }
        return false;
    }
    public boolean eq(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val==((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val==((MyDouble)y).val;
        }
        return false;
    }
    public boolean gt(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val>((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val>((MyDouble)y).val;
        }
        return false;
    }
    public boolean lt(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return val<((MyInt)y).val;
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return (double)val<((MyDouble)y).val;
        }
        return false;
    }
    public double exp(double x, double y)
    {
        return Math.exp(y*(Math.log(x)));
    }
    public MyNumber exp(MyNumber y)
    {
        int i;
        int xi;
        double xd,yd;
//         if(y.getClass().getName().equals("MyInt")){
        if(y.isKind("myint")){
            int yy=((MyInt)y).val;
            if(yy==0) return new MyInt(1);
            if(yy>0){
                xi=1;
                for(i=0;i<yy;i++) xi=xi*val;
                return new MyInt(xi);
            }
            if(yy<0){
                xd=1.0;
                for(i=0;i<-yy;i++) xd=xd/val;
                return new MyDouble(xd);
            }
            return null;
        }
//        if(y.getClass().getName().equals("MyDouble")){
        if(y.isKind("mydouble")){
            yd=((MyDouble)y).val;
            if(yd==0.0) new MyInt(1);
            if(val<=0) return null;
            double r=exp((double)val,yd);
            return new MyDouble(r);
        }
        return null;
   }
    public MyNumber div(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return new MyInt(val/((MyInt)y).val);
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return new MyDouble(((double)val)/((MyDouble)y).val);
        }
        return null;
    }
    public MyInt()
    {
    }
    public MyNumber mul(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return new MyInt(val*((MyInt)y).val);
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return new MyDouble((double)val*((MyDouble)y).val);
        }
        return null;
    }
    public MyNumber sub(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            return new MyInt(val-((MyInt)y).val);
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return new MyDouble((double)val-((MyDouble)y).val);
        }
        return null;
    }
    public MyNumber add(MyNumber y)
    {
//        if(y.getClass().getName().equals("MyInt")){
    	if(y.isKind("myint")){
            int r=val+((MyInt)y).val;
            return new MyInt(r);
        }
//        if(y.getClass().getName().equals("MyDouble")){
    	if(y.isKind("mydouble")){
            return new MyDouble((double)val+((MyDouble)y).val);
        }
        return null;
    }
    public int val;
    public MyInt(int x)
    {
        val=x;
    }
}


