package org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/* */
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
/* 
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
*/
import org.yamalab.android.AdkWikiConnector.pukiwikiCommunicator.language.HtmlTokenizer;

import android.util.Log;

public class PukiWikiConnectorService {
	private static final String TAG = "PukiWikiConnectorService";
	private String baseUrl;
	private String pageName;
	private String pageCharSet;
	private String charset;

    private Properties setting;
	private String messageTextArea;
	private boolean showMessage;
	private String urlField;
	private boolean authInputFlag;
	private boolean loginButtonPressed;
	private PukiwikiJavaApplication application;
    private String loginId;
    private String loginPassword;
	boolean authDialog=false;
	boolean authDialogBack=false;

	public PukiWikiConnectorService(PukiwikiJavaApplication service) {
		Log.d(TAG,"PukiWikiConnectorService");
		this.application=service;
		this.println("saveButtonDebugFrame");
		charset="UTF-8";
		this.setting=new Properties();
	}
	
	private void saveButtonActionPerformed() {
//		System.out.println("saveButton.actionPerformed, event="+evt);
		//TODO add your code for saveButton.actionPerformed
		this.editPageButtonActionPerformed();
		this.updateButtonActionPerformed();
	}
	private void saveText(String x){
		Log.d(TAG,"saveText: "+x);
		this.editPageButtonActionPerformed();
		this.replaceTextWith(x);
	}
	private void saveText(String url, String xx){
		Log.d(TAG,"saveText: editUrl="+url+" text="+xx+".\n");
		this.urlField=url;
		String x=connectToURL(url);
		if(x==null){
			this.application.sendCommandToActivity("activity show connector", "");
			this.application.sendCommandToActivity("connector setMessage", "confirm url and login, pw");
			println("connect error. when writing.");
			return;
		}
		
		// extract charSet from <?xml= ...?> which contains charSet;
		String firstXmltag=getBetween(x,"<?xml","?>");
		if(firstXmltag==null) {
			println("firstXmlTag==null");
			return;
		}
		this.pageCharSet=this.getTagProperty(firstXmltag,"encoding");
		if(this.pageCharSet==null)
			this.pageCharSet="UTF-8";
		this.println("pageCharSet="+this.pageCharSet);
		x=getBetween(x,"<body>","</body>");
		
		String headerPart=getBetween(x,"<div id=\"header\">","</div>");
		String pageNamePart=getBetween(headerPart,"<span class=\"small\">","</span>");
		StringTokenizer st=new StringTokenizer(pageNamePart,"?");
		this.baseUrl=st.nextToken();
		this.pageName=st.nextToken();
		this.editPageButtonActionPerformed();
		this.replaceTextWith(xx);
	}
	
	private String getUrlWithoutParameters(String url){
		if(url==null) return null;
		int i=url.indexOf("?");
		if(i<0) return url;
		String rtn=url.substring(0,i);
		return rtn;
	}
	
//	HttpClient client=null;
	DefaultHttpClient client=null;
	
	String currentUrl;
	private String connectToURL(String url) {
		Log.d(TAG,"connectToURL("+url+")");
		currentUrl=url;
		//TODO add your code for connectButton.actionPerformed
		String pageText=null;
//		client=new HttpClient();
		client=new DefaultHttpClient();
//		this.messageTextArea.append(url+"\n");
		if(this.authDialog){
     	   return this.connectToURLWithAuth(url);
 		}
		try{
			this.println("new getMethod("+url+")");
//    		HttpMethod method = new GetMethod(url);
			HttpGet method = new HttpGet(url);

//	    	method.getParams().setContentCharset("UTF-8");
//			int status=client.executeMethod(method);
			pageText= client.execute(
					method,
					new ResponseHandler<String>(){
				        @Override
				        public String handleResponse(HttpResponse response)
				                throws ClientProtocolException, IOException {
				            // response.getStatusLine().getStatusCode()でレスポンスコードを判定する。
				            // 正常に通信できた場合、HttpStatus.SC_OK（HTTP 200）となる。
				            switch (response.getStatusLine().getStatusCode()) {
				            case HttpStatus.SC_OK:
				                // レスポンスデータを文字列として取得する。
				                // byte[]として読み出したいときはEntityUtils.toByteArray()を使う。
				                return EntityUtils.toString(response.getEntity(), "UTF-8");
				            case HttpStatus.SC_NOT_FOUND:
				                throw new RuntimeException("データないよ！"); //FIXME
				            
				            default:
				            	String status=(response.getStatusLine()).toString();
			    		        println("Method failed: " + status);
			    		        if(status.indexOf("401")>=0){
			    		      	     application.sendCommandToActivity("connector loginRequired", "");
			    		      	     application.sendCommandToActivity("connector setLoginMessage",  "Login:"+ currentUrl);
			                         println("connectToUrl("+currentUrl+")...Method faile"+status);
			    		        }
				                throw new RuntimeException(status); //FIXME
				            }

				        }
				    }
						
				);
			return pageText;
			/*
		    if (status != HttpStatus.SC_OK) {
		        this.println("Method failed: " + method.getStatusLine());
		        if((method.getStatusLine()).toString().indexOf("401")>=0){
		      	     application.sendCommandToActivity("connector loginRequired", "");
		      	     application.sendCommandToActivity("connector setLoginMessage",  "Login:"+ currentUrl);
                     println("connectToUrl("+url+")...Method faile"+status);
                      return null;
		        }
		    }
		    else{
		    	pageText=this.getText(method);
				return pageText;
		    }
		    */
		}
		catch(Exception e){
			this.println(e.toString()+"\n");
			e.printStackTrace();
		}
		return null;
	}
	
	private String connectToURLWithAuth(String url) {
		Log.d(TAG,"connectToURLWithAuth("+url+")");
		//TODO add your code for connectButton.actionPerformed
   	    this.authDialog=true;
  	    this.currentUrl=url;
		String urlWithoutParameters=getUrlWithoutParameters(currentUrl);
		if(urlWithoutParameters==null){
			application.sendCommandToActivity("connector setMessage", "connect failed. no Url is specified.");			
		}
  	    this.setting.setProperty("auth-url", urlWithoutParameters);
  	    application.sendCommandToActivity("connector setAuthUrl-",urlWithoutParameters);
	    String authUrl="basicAuth-"+urlWithoutParameters;
//	    Log.d(TAG,"connectToURL-authUrl="+authUrl);
	    String idPass=setting.getProperty(authUrl);
//	    Log.d(TAG,"connectToURL-idPass="+idPass);
       	String id="";
       	String pas="";
	    if(idPass!=null){
	        Log.d(TAG,"connectToUR-login..idPass!=null");
	    	int bp=idPass.indexOf("::::");
	       	id=idPass.substring(0,bp);
//	    	Log.d(TAG,"connectToUR-login..id="+id);
	       	pas=idPass.substring(bp+"::::".length());
//	    	Log.d(TAG,"connectToUR-login..pass="+pas);
       	    if(id.equals("")){
				application.sendCommandToActivity("connector setMessage", "connect failed. no login id.");
	            return null;
	        }
	        if(pas.equals("")){
				application.sendCommandToActivity("connector setMessage", "connect failed. no password.");
	            return null;
	        }
	    }
	    else{
			application.sendCommandToActivity("connector setMessage", "connect failed. no id, password.");
			this.authDialog=false;
	        return null;
	    }
		this.println("authDialog is not null");
		String registeredUrl=setting.getProperty("auth-url");
		this.println("urlWithoutParamaters="+urlWithoutParameters);
		this.println("registeredUrl="+registeredUrl);
		this.println("authDialog is not null");
		String pageText=null;
		if(registeredUrl==null){
			this.application.sendCommandToActivity("connector setMessage", "No login Id, pass.");
			return null;
		}
		if(registeredUrl.equals(urlWithoutParameters)){
			this.println("registeredUrl == urlWithoutParameters");
//			client.getParams().setAuthenticationPreemptive(true);
		    // 認証情報(ユーザ名とパスワード)の作成.
    	    this.setting.setProperty(authUrl,idPass);
		}
//		HttpMethod method=null;
		HttpGet method=null;
		try{
			Credentials defaultcreds1 = new UsernamePasswordCredentials(id,pas);
		    // 認証のスコープ.
	        AuthScope scope1 = new AuthScope(null, -1, null);
		    // スコープと認証情報の組合せをセット.
//	    client.getState().setCredentials(scope1, defaultcreds1);				
			this.println("new getMethod("+url+")");
//    		method = new GetMethod(url);
//        	method.setDoAuthentication(true);
			client.getCredentialsProvider().setCredentials(
					scope1, 
					defaultcreds1);
    	}
   		catch(Exception e){
   			this.println(e.toString()+"\n");
   			e.printStackTrace();
   		}
//	    	method.getParams().setContentCharset("UTF-8");
		try{
//		    int status=client.executeMethod(method);
			method = new HttpGet(url);
/*
 			HttpResponse status=client.execute(method);
		    if (status != HttpStatus.SC_OK) {
		          this.println("Method failed: " + method.getStatusLine());
		          if((method.getStatusLine()).toString().indexOf("401")>=0){
		     			this.println("auth login...error..");		        	  
		          }

		    }
	    	pageText=this.getText(method);
			return pageText;
			*/
			pageText= client.execute(
					method,
					new ResponseHandler<String>(){
				        @Override
				        public String handleResponse(HttpResponse response)
				                throws ClientProtocolException, IOException {
				            // response.getStatusLine().getStatusCode()でレスポンスコードを判定する。
				            // 正常に通信できた場合、HttpStatus.SC_OK（HTTP 200）となる。
				            switch (response.getStatusLine().getStatusCode()) {
				            case HttpStatus.SC_OK:
				                // レスポンスデータを文字列として取得する。
				                // byte[]として読み出したいときはEntityUtils.toByteArray()を使う。
				                return EntityUtils.toString(response.getEntity(), "UTF-8");
				            case HttpStatus.SC_NOT_FOUND:
				                throw new RuntimeException("データないよ！"); //FIXME
				            
				            default:
				            	String status=(response.getStatusLine()).toString();
			    		        println("Method failed: " + status);
			    		        if(status.indexOf("401")>=0){
			    		      	     application.sendCommandToActivity("connector loginRequired", "");
			    		      	     application.sendCommandToActivity("connector setLoginMessage",  "Login:"+ currentUrl);
			                         println("connectToUrl("+currentUrl+")...Method faile"+status);
			    		        }
				                throw new RuntimeException(status); //FIXME
				            }

				        }
				    }
						
				);
			}
		catch(Exception e){
   			this.println("auth login...executeMethod error"+e.toString()+"\n");
   			e.printStackTrace();			
   			return null;
		}
		this.messageTextArea=this.messageTextArea+pageText;
		return pageText;
	}

//	private String getText(HttpMethod method){
	/*
	private String getText(HttpGet method){
		String pageText="";
	    try{
		   InputStream is=method.getResponseBodyAsStream();
 		   InputStreamReader isr=new InputStreamReader(is,this.charset);
	       BufferedReader br=new BufferedReader(isr);
	       String line="";
	       pageText="";
	       while(true){
	    	   line=br.readLine();
	    	   pageText=pageText+line+"\n";
	        	if(line==null) break;
	    	    this.messageTextArea=this.messageTextArea+ line+"\n";
//	    	    System.out.println(line);
	       }
	       method.releaseConnection();		
	    }
	    catch(Exception e){}
	    return pageText;
	}
*/	
	private void sendButtonActionPerformed() {
//		this.println("sendButton.actionPerformed, event="+evt);
		//TODO add your code for sendButton.actionPerformed
	}
	String updateText;
	String actionUrl;
	String editCmd;
	String editPage;
	String editDigest;
	String editWriteValue;
	String editEncodeHint;
    String editUrlText;
	private void editPageButtonActionPerformed() {
		Log.d(TAG,"editPageButtonActionPerformed");
//		this.println("editPageButton.actionPerformed, event="+evt);
		//TODO add your code for editPageButton.actionPerformed
		   String editUrl=baseUrl+"?cmd=edit&page="+pageName;
		this.println("editUrl="+editUrl);
//		this.messageTextArea.append(baseUrl+"\n");
		this.println(baseUrl+"\n");
//		this.messageTextArea.append(editUrl+"\n");
		this.println(editUrl+"\n");
		editUrlText=editUrl;
        urlField=editUrlText;
   		messageTextArea="";
		String x=this.connectToURL(editUrl);
		if(x==null){
			this.application.sendCommandToActivity("activity showConnector", "");
			println("connect error.");
			return;
		}

//		String x=this.messageTextArea.getText();
		
		/* get the first form from the url*/
		String form=this.getBetween(x,"<form", "</form>");
		if(form==null) return;
		this.println("form="+form);
		/* get the head part in the text area from the form*/
		int i=form.indexOf("<textarea ");
		if(i<0) {
			this.println("Could not find out textarea");
			return;
		}
//		this.messageTextArea.setText("");
//		this.messageTextArea.append("i="+i+"\n");
		String y="";
		String z="";
		try{
		    y=form.substring(i);
		    z=y.substring(y.indexOf(">")+1);		
		}
		catch(Exception e){
			
		}
		this.println("z="+z);
//		this.println("plugInName="+this.plugInName);
//		int j=z.indexOf("#"+this.plugInName);
		int j=z.indexOf("result:");
		if(j<0){
			this.println("Could not find out result:");
			return;
		}
		int k=j+("result:").length();

		//String head=x.substring(0,k-1);
		this.println("j="+j+" k="+k);
		// has the command #netpaint argument? 
		String dataStart=z.substring(j);
		this.println("dataStart="+dataStart);
		/*
		if(dataStart.startsWith("result:")){
			String theCommand="result:" ;
			this.println("theCommand="+theCommand);
			k=j+theCommand.length();
		}
		*/
		this.println("j="+j+" k="+k);
		
		String head=z.substring(0,k);
		//
        head=head.replaceAll("&quot;", "\"");
        head=head.replaceAll("&lt;", "<");
        head=head.replaceAll("&gt;", ">");		
		//
		String w=z.substring(head.length());
		int l=w.indexOf("</textarea");
		String tail=w.substring(l);
		String body=w.substring(0,l-1);
		this.println("head="+head);
		this.println("body="+body);
		this.println("tail="+tail);
		this.updateText=head;
		String actionwork1=form.substring(0,form.indexOf(">"));
		this.println("actionwork1="+actionwork1);
		this.actionUrl=this.getTagProperty(actionwork1, "action");
		this.println("action url="+this.actionUrl);
		
		/* 
		 getting input properties
		 */
		HtmlTokenizer htmlt=new HtmlTokenizer(form);
		while(htmlt.hasMoreTokens()){
			String t=htmlt.nextToken();
			if(t.startsWith("<input")){
				String ttype=getTagProperty(t,"type");
				if(ttype.equals("hidden")){
				   String tname=getTagProperty(t,"name");
				   String tvalue=getTagProperty(t,"value");
				   this.println(" "+tname+"="+tvalue);
				   if(tname.equals("cmd")){
					   this.editCmd=tvalue;
				   }
				   if(tname.equals("page")){
					   this.editPage=tvalue;
				   }
				   if(tname.equals("digest")){
				   		this.editDigest=tvalue;
				   }
				   if(tname.equals("encode_hint")){
					   this.editEncodeHint=tvalue;
				   }
				}
				if(ttype.equals("submit")){
				   String tname=getTagProperty(t,"name");
				   String tvalue=getTagProperty(t,"value");
				   this.println(" "+tname+"="+tvalue);
				   if(tname.equals("write")){
						this.editWriteValue=tvalue;
			       }
				}
			}
		}
		
	}	
	private void updateButtonActionPerformed() {
		Log.d(TAG,"updateButtonActionPerformed");
		String output=application.getOutput();
		replaceTextWith(output);
	}
	String insertSpaceAfterNewLine(String x){
		StringTokenizer st=new StringTokenizer(x,"\n",true);
		String rtn="";
		while(st.hasMoreTokens()){
			String t=st.nextToken();
			if(t.equals("\n")){
				rtn=rtn+t+" ";
			}
			else{
				rtn=rtn+t;
			}
		}
		return rtn;
	}
	
	private String getTagProperty(String tag, String key){
//		System.out.println("tag="+tag);
//		System.out.println("key="+key);
		StringTokenizer st=new StringTokenizer(tag," =",true);
		String t=st.nextToken();
//		System.out.println(" first token="+t);
		while(st.hasMoreTokens()){
			t=st.nextToken();
			if(t.equals(" ")){        // skip space
				while(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			}
			String keyx=t;
//			System.out.println(" key?="+keyx);
			if(!st.hasMoreTokens()) return "";
			t=st.nextToken();
			if(t.equals(" ")){        // skip space
				while(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			}
			if(t.equals("=")){
//				System.out.println("...=");
				if(!st.hasMoreTokens()) return "";
				t=st.nextToken();
				if(t.equals(" ")){
					if(!st.hasMoreTokens()) return "";
					t=st.nextToken();
				}
			    if(keyx.equals(key)){
//			    	System.out.println(" keyx="+key+" t="+t);
			    	if(t.startsWith("\"")){
			    			t=t.substring(1);
			    	}
			    	if(t.endsWith("\"")){
			    		t=t.substring(0,t.length()-1);
			    	}
			    	return t;
			    }
			}
			
		}
		return "";
	}
	
	private void replaceTextWith(String x){
		// System.out.println("updateButton.actionPerformed, event="+evt);
		//TODO add your code for updateButton.actionPerformed
		
		/* make the body (fig) */
		/*
		try{
			fig=new String(fig.getBytes("UTF-8"),  "UTF-8");
		}
		catch(Exception e){
			return;
		}
		*/
		this.updateText=this.updateText+"\n "+insertSpaceAfterNewLine(x);
		
		this.urlField=this.actionUrl;
		String url=this.urlField;
		this.println("url="+url);
//		this.messageTextArea.append(this.updateText);
		   BufferedReader br = null;
//		System.out.println("updateText="+this.updateText);
		this.println("editWriteValue="+this.editWriteValue);
		this.println("editCmd="+this.editCmd);
		this.println("editPage="+this.editPage);
		this.println("digest="+this.editDigest);
		try{
//    		PostMethod method = new PostMethod(url);
			HttpPost method = new HttpPost(url);
    		if(this.client==null) return;
//    		method.getParams().setContentCharset(this.pageCharSet);
    		/*
    		method.setParameter("msg",this.updateText);
//    		method.setParameter("encode_hint",this.editEncodeHint);
    		method.addParameter("write",this.editWriteValue);
    		method.addParameter("cmd",this.editCmd);
    		method.addParameter("page",this.editPage);
    		method.addParameter("digest",this.editDigest);
    		*/
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
    		params.add(new BasicNameValuePair("msg", this.updateText));
    		params.add(new BasicNameValuePair("encode_hint", this.editEncodeHint));
    		params.add(new BasicNameValuePair("write", this.editWriteValue));
    		params.add(new BasicNameValuePair("cmd", this.editCmd));
    		params.add(new BasicNameValuePair("page", this.editPage));
    		params.add(new BasicNameValuePair("digest", this.editDigest));
    		method.setEntity(new UrlEncodedFormEntity(params));


//			int status=client.executeMethod(method);
    		/*
    		int status=client.execute(method);
		    if (status != HttpStatus.SC_OK) {
		          this.println("Method failed: " + method.getStatusLine());
		          method.getResponseBodyAsString();
		    }
		    else {
		          br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
		          String readLine;
		          while(((readLine = br.readLine()) != null)) {
		            this.println(readLine);
		          }
		   }
		   */
		   String status= client.execute(
					method,
					new ResponseHandler<String>(){
			     		BufferedReader br = null;
				        @Override
				        public String handleResponse(HttpResponse response)
				                throws ClientProtocolException, IOException {
				            // response.getStatusLine().getStatusCode()でレスポンスコードを判定する。
				            // 正常に通信できた場合、HttpStatus.SC_OK（HTTP 200）となる。
				            switch (response.getStatusLine().getStatusCode()) {
				            case HttpStatus.SC_OK:
				                // レスポンスデータを文字列として取得する。
				                // byte[]として読み出したいときはEntityUtils.toByteArray()を使う。
				            	InputStream content = response.getEntity().getContent();
						          br = new BufferedReader(new InputStreamReader(content));
						          String readLine;
						          while(((readLine = br.readLine()) != null)) {
						            println(readLine);
						          }
						          content.close();
						          br.close();
						          return response.getStatusLine().toString();
//				                return EntityUtils.toString(response.getEntity(), "UTF-8");
				            case HttpStatus.SC_NOT_FOUND:
				                throw new RuntimeException("データないよ！"); //FIXME
				            
				            default:
				            	String status=(response.getStatusLine()).toString();
			    		        println("Method failed: " + status);
			    		        if(status.indexOf("401")>=0){
			    		      	     application.sendCommandToActivity("connector loginRequired", "");
			    		      	     application.sendCommandToActivity("connector setLoginMessage",  "Login:"+ currentUrl);
			                         println("connectToUrl("+currentUrl+")...Method faile"+status);
			    		        }
				                throw new RuntimeException(status); //FIXME
				            }

				        }
				    }
						
				);
//			method.releaseConnection();
		   client.getConnectionManager().shutdown();
		}
		catch(Exception e){
//			this.messageTextArea.append(e.toString()+"\n");
			System.out.println(""+e);
			e.printStackTrace();
		}
		
	}
	
	boolean isInStringConst(String x, int p){
		
		int px=0;
		int py=0;
		boolean isIn=false;
		while(px<x.length()){
			if(px>p) return false;
			char cx=x.charAt(px);
			char cy=0;
			py=px+1;
			if(cx=='"'){
				isIn=true;
				while(py<x.length()){
					cy=x.charAt(py);
					if(cy=='"'){
						if(px<p && p<py)
							return true;
						else{
							isIn=false;
							px=py;
							break;
						}
					}
					if(cy=='\\'){
						py=py+1;
					}
					py=py+1;
				}
				if(isIn)
					return true;
			}
			if(cx=='\\'){
				px=px+1;
			}
			px=px+1;
		}
		return false;
	}
	String inputText;
	private void readFromPukiwikiPageAndSetData(String url){
		Log.d(TAG,"readFromPukiwikiPageAndSetData("+url+")");
		this.println("editUrl="+url+"\n");
		this.urlField=url;
		String x=connectToURL(url);
		if(x==null){
			this.application.sendCommandToActivity("activity showConnector", "");
			this.application.sendCommandToActivity("connector setMessage","connection error. confirm url and login");
			println("connect error. when reading.");
			return;
		}
		
		// extract charSet from <?xml= ...?> which contains charSet;
		String firstXmltag=getBetween(x,"<?xml","?>");
		if(firstXmltag==null) {
			println("firstXmlTag==null");
			return;
		}
		this.pageCharSet=this.getTagProperty(firstXmltag,"encoding");
		if(this.pageCharSet==null)
			this.pageCharSet="UTF-8";
		this.println("pageCharSet="+this.pageCharSet);
		x=getBetween(x,"<body>","</body>");
		
		// exclude until <applet
//		int i=x.indexOf("<applet");
//		if(i<0) return;
//		x=x.substring(i);
		String headerPart=getBetween(x,"<div id=\"header\">","</div>");
		String pageNamePart=getBetween(headerPart,"<span class=\"small\">","</span>");
		StringTokenizer st=new StringTokenizer(pageNamePart,"?");
		this.baseUrl=st.nextToken();
//		System.out.println("baseUrl="+baseUrl);
		this.pageName=st.nextToken();
//		System.out.println("pageName="+pageName);
		// extract <pre>...</pre> where the figure is.
		String inw=getBetween(x,"<pre>","</pre>");
		String input=inw;
		if(input==null) return;
        input=input.replaceAll("&quot;", "\"");
        input=input.replaceAll("&lt;", "<");
        input=input.replaceAll("&gt;", ">");
        inputText=input;
        application.setInput(inputText+"\n");       			
	}

	/*
	 *  get the first string which is in from l to r in the x
	 */
	String getBetween(String x, String l, String r){
//		System.out.println("x="+x);
		this.println("l="+l);
		this.println("r="+r);
		int i=0;
		while(i<=0){
			i=x.indexOf(l,i);
    		if(i<0) return null;
    		if(i==0) break;
	    	if(isInStringConst(x,i)){
		    	i=i+l.length();
		    }
		}
		
		i=i+l.length();
		int j=i;
		while(j<=i){
		    j=x.indexOf(r,j);
		    if(j<0) return null;
		    if(isInStringConst(x,j)){
		    	j=j+r.length();
		    }
		}
		String rtn="";
		try{
			rtn=x.substring(i,j);
		}
		catch(Exception e){
			return null;
		}
//		System.out.println("rtn="+rtn);
		return rtn;
	}
	private void println(String x){
		Log.d(TAG,"println("+x+")");
//		if(showMessage) return;
			/* */
		if(this.application!=null){
				application.sendCommandToActivity("activity append message", x);
		}
//			this.messageTextArea.setCaretPosition((this.messageTextArea.getText()).length());
			/* */
//		System.out.println(x);
	}

	private void setUrl(String x){
		this.urlField=x;
	}
	String tempAuth;
	public boolean parseCommand(String subcmd, String v){
		String [] rest=new String[1];
		String cmd=skipSpace(subcmd);
		Log.d(TAG,"parseCommand-"+cmd);
		if(parseKeyWord(cmd,"connect-",rest)){ // connector connect
			String x=this.connectToURL(v);
			if(x==null){
				this.application.sendCommandToActivity("activity showConnector", "");
			}
			return true;
		}	
		else
		if(parseKeyWord(cmd,"login-",rest)){ // connector login
			this.loginButtonPressed=true;
			String baseUrl=getUrlWithoutParameters(this.urlField);
			if(baseUrl==null){
				return false;
			}
			this.setting.setProperty("auth-url",baseUrl);
		    String authUrl="basicAuth-"+baseUrl;
//		    Log.d(TAG,"parseCommand-login, authUrl="+authUrl);
		    String id_pw=this.loginId+"::::"+this.loginPassword;
//		    Log.d(TAG,"parseCommand-login, id-pw="+id_pw);
			this.setting.setProperty(authUrl,id_pw);
			this.authDialog=true;
			this.readFromPukiwikiPageAndSetData(this.urlField);
			return true;
		}
		else
		if(parseKeyWord(cmd,"loginCancel-",rest)){
			this.loginButtonPressed=false;
			return true;
		}
		else
		if(parseKeyWord(cmd,"message-",rest)){ // connector message
			this.println(v);
			return true;
		}
		else
		if(parseKeyWord(cmd,"read-",rest)){ // connector read
			this.readFromPukiwikiPageAndSetData(this.urlField);
			return true;
		}		
		else
		if(parseKeyWord(cmd,"readPage-",rest)){ // connector readPage
			String baseUrl=this.getUrlWithoutParameters(this.urlField);
			if(baseUrl==null){
				return false;
			}
			this.readFromPukiwikiPageAndSetData(baseUrl+"?"+v);
			return true;
		}		
		else
		if(parseKeyWord(cmd,"replaceTextWith-",rest)){
			this.replaceTextWith(v);
			return true;
		}
		else
		if(parseKeyWord(cmd,"saveText-",rest)){
			this.saveText(v);
			return true;
		}
		else
		if(parseKeyWord(cmd,"saveTextAtUrl-",rest)){
			String xurl=rest[0];
			this.saveText(xurl, v);
			return true;
		}
		else
		if(parseKeyWord(cmd,"setShowMessage-",rest)){
			String x=v;
			if(x.equals("true"))
				this.showMessage=true;
			else
				this.showMessage=false;
			return true;
		}
		else
		if(parseKeyWord(cmd,"setLoginId-",rest)){
			this.loginId=v;
			return true;
		}
		else
		if(parseKeyWord(cmd,"setLoginPassword-",rest)){
			this.loginPassword=v;
			return true;
		}
		else
		if(parseKeyWord(cmd,"setIdPass-",rest)){
			if(this.tempAuth!=null)
			this.setting.put(tempAuth, v);
			return true;
		}
		else
		if(parseKeyWord(cmd,"setAuthUrl-",rest)){
			this.setting.put("auth-Url", v);
			String urlWithoutParameters=getUrlWithoutParameters(currentUrl);
			if(urlWithoutParameters==null){
				return false;
			}
//	  	  this.authDialog.setProperty("auth-url", urlWithoutParameters);
	  	    this.setting.setProperty("auth-url", urlWithoutParameters);

			return true;
		}
		else
		if(parseKeyWord(cmd,"setAuth2Url-",rest)){
			this.tempAuth=v;
			return true;
		}
		else
		if(parseKeyWord(cmd,"setUrl-",rest)){
			this.setUrl(v);
			return true;
		}
		return false;
	}
	String skipSpace(String x){
		while(x.startsWith(" ")) x=x.substring(1);
		return x;
	}
	boolean parseKeyWord(String x, String key, String [] rest){
		if(x.startsWith(key)){
			rest[0]=x.substring(key.length());
			return true;
		}
		return false;
	}
	boolean parseInt(String x, int[] intrtn, String [] rest){
		char c=x.charAt(0);
		String ix="";
		while('0'<=c && c<='9'){
			ix=ix+c;
			x=x.substring(1);
			c=x.charAt(0);
		}
		if(!ix.equals("")){
		   int ixx=(new Integer(ix)).intValue();
		   intrtn[0]=ixx;
		   rest[0]=x;
		   return true;
		}
		return false;		
	}
	boolean parseStrConst(String x,String [] sconst, String [] rest){
		String xconst="";
		if(x.startsWith("\"")){
			x=x.substring(1);
			while(!x.startsWith("\"")){
				if(x.length()<1) return false;
				if(x.startsWith("\\")){
						xconst=xconst+x.charAt(0)+x.charAt(1);
						x=x.substring(2);
				}
				else{
					xconst=xconst+x.charAt(0);
					x=x.substring(1);
				}
			}
			x=x.substring(1);
			sconst[0]=xconst;
			rest[0]=x;
			return true;
		}
		return false;
	}
}
