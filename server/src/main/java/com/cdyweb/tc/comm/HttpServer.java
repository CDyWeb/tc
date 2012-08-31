package com.cdyweb.tc.comm;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;

public class HttpServer {

  private String httpdocs=null;
  private static HttpQueryHandler queryHandler=null;
  
  private static HttpServer instance=null;
    
  private HttpServer () {
    // >>> HttpServer Constructor is Private
  }
    
  public static HttpServer getInstance() {
    if (instance==null) instance=new HttpServer();
    return instance;
  }
  
  public static void main(String[] args) throws Exception {
    HttpServer server=new HttpServer();
    if (args.length < 1) {
      //System.err.println("Please specify document root directory");
      //System.exit(1);
      File directory = new File(".");
      server.httpdocs=directory.getAbsolutePath();
    } else {
      server.httpdocs=args[0];
    }
    server.start(null);
  }

  public String getHttpdocs() {
    return httpdocs;
  }

  public void setHttpdocs(String httpdocs) {
    this.httpdocs = httpdocs;
  }

  public static HttpQueryHandler getQueryHandler() {
    return queryHandler;
  }
  
  public void start(HttpQueryHandler h) throws IOException {
    queryHandler=h;
    Thread t = new RequestListenerThread(8080, httpdocs);
    t.setDaemon(false);
    t.start();
  }

  static class HttpFileHandler implements HttpRequestHandler {

    private final String docRoot;

    public HttpFileHandler(final String docRoot) {
      super();
      this.docRoot = docRoot;
    }

    public void handle(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

      String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
      if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
        throw new MethodNotSupportedException(method + " method not supported");
      }
      String target = request.getRequestLine().getUri();
      
      List post=null;
      if (request instanceof HttpEntityEnclosingRequest) {
        HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
        String entityContent = EntityUtils.toString(entity);
        System.out.println("Incoming post: " + entityContent);
        post=org.apache.http.client.utils.URLEncodedUtils.parse(entityContent, Charset.defaultCharset());
      }

      String[] q = target.split("\\?");
      if (q.length==2) {
        target=q[0];
        List list=org.apache.http.client.utils.URLEncodedUtils.parse(q[1], Charset.defaultCharset());
        if (queryHandler!=null) {
          String responseString;
          if (post==null) responseString=queryHandler.query_get(target, list);
          else responseString=queryHandler.query_post(target, list, post);
          response.setStatusCode(HttpStatus.SC_OK);
          StringEntity body = new StringEntity(responseString, ContentType.create("text/html", (Charset) null));
          response.setEntity(body);
          System.out.println("Serving from queryHandler: " + target);
          return;
        }
      }

      final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
      if (!file.exists()) {

        response.setStatusCode(HttpStatus.SC_NOT_FOUND);
        StringEntity entity = new StringEntity(
                "<html><body><h1>File" + file.getPath()
                + " not found</h1></body></html>",
                ContentType.create("text/html", "UTF-8"));
        response.setEntity(entity);
        System.out.println("File " + file.getPath() + " not found");

      } else if (!file.canRead() || file.isDirectory()) {

        response.setStatusCode(HttpStatus.SC_FORBIDDEN);
        StringEntity entity = new StringEntity(
                "<html><body><h1>Access denied</h1></body></html>",
                ContentType.create("text/html", "UTF-8"));
        response.setEntity(entity);
        System.out.println("Cannot read file " + file.getPath());

      } else {

        response.setStatusCode(HttpStatus.SC_OK);
        String contentType="text/html";
        String[] s=target.split("\\.");
        String ext=s[s.length-1];
        if (ext.equals("css")) contentType="text/css";
        if (ext.equals("js")) contentType="text/javascript";
        if (ext.equals("jpg")) contentType="image/jpeg";
        if (ext.equals("gif")) contentType="image/gif";
        if (ext.equals("png")) contentType="image/png";
        FileEntity body = new FileEntity(file, ContentType.create(contentType, (Charset) null));
        response.setEntity(body);
        System.out.println("Serving file " + file.getPath());
      }
    }
  }

  static class RequestListenerThread extends Thread {

    private final ServerSocket serversocket;
    private final HttpParams params;
    private final HttpService httpService;

    public RequestListenerThread(int port, final String docroot) throws IOException {
      this.serversocket = new ServerSocket(port);
      this.params = new SyncBasicHttpParams();
      this.params
              .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
              .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
              .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
              .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
              .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

      // Set up the HTTP protocol processor
      HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]{
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
              });

      // Set up request handlers
      HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
      reqistry.register("*", new HttpFileHandler(docroot));

      // Set up the HTTP service
      this.httpService = new HttpService(
              httpproc,
              new DefaultConnectionReuseStrategy(),
              new DefaultHttpResponseFactory(),
              reqistry,
              this.params);
    }

    @Override
    public void run() {
      System.out.println("Listening on port " + this.serversocket.getLocalPort());
      while (!Thread.interrupted()) {
        try {
          // Set up HTTP connection
          Socket socket = this.serversocket.accept();
          DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
          System.out.println("Incoming connection from " + socket.getInetAddress());
          conn.bind(socket, this.params);

          // Start worker thread
          Thread t = new WorkerThread(this.httpService, conn);
          t.setDaemon(true);
          t.start();
        } catch (InterruptedIOException ex) {
          break;
        } catch (IOException e) {
          System.err.println("I/O error initialising connection thread: "
                  + e.getMessage());
          break;
        }
      }
    }
  }

  static class WorkerThread extends Thread {

    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public WorkerThread(
            final HttpService httpservice,
            final HttpServerConnection conn) {
      super();
      this.httpservice = httpservice;
      this.conn = conn;
    }

    @Override
    public void run() {
      System.out.println("New connection thread");
      HttpContext context = new BasicHttpContext(null);
      try {
        while (!Thread.interrupted() && this.conn.isOpen()) {
          this.httpservice.handleRequest(this.conn, context);
        }
      } catch (ConnectionClosedException ex) {
        System.err.println("Client closed connection");
      } catch (IOException ex) {
        System.err.println("I/O error: " + ex.getMessage());
      } catch (HttpException ex) {
        System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
      } finally {
        try {
          this.conn.shutdown();
        } catch (IOException ignore) {
        }
      }
    }
  }
}
