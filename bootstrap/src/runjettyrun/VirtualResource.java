package runjettyrun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.mortbay.resource.FileResource;
import org.mortbay.resource.Resource;

public class VirtualResource extends Resource {

	/**
	 *
	 */
	private static final long serialVersionUID = 8748988779292501912L;

	private Resource resource = null;
	private URL url = null;
	private String resourcePath;
	private String resourcebase = null;

	/**
	 *
	 * @param contextPath must start with "/"
	 * @param path
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public VirtualResource(String contextPath,
			String path) throws MalformedURLException, IOException, URISyntaxException {

//		System.out.println("registing ["+contextPath+"] to ["+ path+"]");
		this.resourcePath = path;
		url = new File(path).toURI().toURL();
		resource = new FileResource(url);
		resourcebase = contextPath;
	}



	public void release() {
		resource.release();
	}


	public boolean exists() {
		return resource.exists();
	}


	public boolean isDirectory() {
		return resource.isDirectory();
	}


	public long lastModified() {
		return resource.lastModified();
	}

	/**
	 * 2011/5/15 Tony:
	 *
	 * Why we have such much unsupported method here?
	 * Because this one is not a delegator to real resource,
	 * since it's a resource related to webapp root .
	 *
	 * So if system try to access this resource , it means "/" ,
	 * not "/<context path>/" , so it could be some troubles.
	 *
	 * We wrote this just for making sure when user try to access
	 * "/<context path>" with the root ResourceCollecction,
	 * the Virtual Resource will bring him to real resource,
	 * and it could help them to do the real operators they need. ;)
	 *
	 * I am not sure if that's a good implementation ,
	 *
	 * but it's better to be honest , we didn't support them so far
	 * and don't know when user will need them at this time.
	 *
	 * If you found a better approach or issues related to this,
	 * you could discuss it with me , tonylovejava[at]gmail.com .
	 */

	public long length() {
		throw new UnsupportedOperationException("Unsupported");
	}


	public URL getURL() {
		throw new UnsupportedOperationException("Unsupported");
	}

	public File getFile() throws IOException {
		throw new UnsupportedOperationException("Unsupported");
	}


	public String getName() {
		throw new UnsupportedOperationException("Unsupported");
	}


	public InputStream getInputStream() throws IOException {
		throw new UnsupportedOperationException("Unsupported");
	}


	public OutputStream getOutputStream() throws IOException, SecurityException {
		throw new UnsupportedOperationException("Unsupported");
	}


	/**
	 * we don't expect Linked Resource will be deleted in this case ,
	 * so just make unsupported in this case.
	 */
	public boolean delete() throws SecurityException {
		throw new UnsupportedOperationException("Unsupported");
	}

	/**
	 * we don't expect Linked Resource be renamed in this case ,
	 * so just make unsupported in this case.
	 */
	public boolean renameTo(Resource dest) throws SecurityException {
		throw new UnsupportedOperationException("Unsupported");
	}

	/**
	 * we don't expect Linked Resource be listed in this case ,
	 * so just make unsupported in this case.
	 */
	public String[] list() {
		throw new UnsupportedOperationException("Unsupported");
	}


	public Resource addPath(String path) throws IOException,
			MalformedURLException {

//		System.out.println("try to addPath on virtual resourcing:"+path);
		if(!path.startsWith("/"))
			path = "/" +path;

		if(path.startsWith(this.resourcebase)){


			if(this.resourcebase.equals(path)){
//				System.out.println("got virtual resource , forward  =>"+resourcePath);
				return resource;
			}else{
//				System.out.println("got virtual resource , forward =>"+
//						resourcePath+"/"+path.substring(this.resourcebase.length()+1));
				return resource.addPath(path.substring(this.resourcebase.length()+1));

			}
		}


		return null;
	}

	/* test code */
	public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException {

		VirtualResource vr = new VirtualResource("/mytest", "C:/test/");
		Resource r = vr.addPath("/mytest/test.txt");
		System.out.println(r.exists());

	}
}
