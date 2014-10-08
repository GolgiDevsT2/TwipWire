//
// This Software (the “Software”) is supplied to you by Openmind Networks
// Limited ("Openmind") your use, installation, modification or
// redistribution of this Software constitutes acceptance of this disclaimer.
// If you do not agree with the terms of this disclaimer, please do not use,
// install, modify or redistribute this Software.
//
// TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED ON AN
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE.
//
// Each user of the Software is solely responsible for determining the
// appropriateness of using and distributing the Software and assumes all
// risks associated with use of the Software, including but not limited to
// the risks and costs of Software errors, compliance with applicable laws,
// damage to or loss of data, programs or equipment, and unavailability or
// interruption of operations.
//
// TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW OPENMIND SHALL NOT
// HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, WITHOUT LIMITATION,
// LOST PROFITS, LOSS OF BUSINESS, LOSS OF USE, OR LOSS OF DATA), HOWSOEVER 
// CAUSED UNDER ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
// WAY OUT OF THE USE OR DISTRIBUTION OF THE SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGES.
//

package io.golgi.twipwire;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;



import com.json.parsers.JsonParserFactory;
import com.json.parsers.JSONParser;
import com.openmindnetworks.golgi.JavaType;
import com.openmindnetworks.golgi.api.GolgiAPI;
import com.openmindnetworks.golgi.api.GolgiAPIHandler;
import com.openmindnetworks.golgi.api.GolgiAPIImpl;
import com.openmindnetworks.golgi.api.GolgiAPINetworkImpl;
import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiTransportOptions;
import com.openmindnetworks.slingshot.ntl.NTL;
import com.openmindnetworks.slingshot.tbx.TBX;

import io.golgi.twipwire.gen.TweetDetails;
import io.golgi.twipwire.gen.TweetFilter;
import io.golgi.twipwire.gen.TwipWireService.newTweet;
import io.golgi.twipwire.gen.TwipWireService.*;


public class Server extends Thread implements GolgiAPIHandler{
    private String devKey = null;
    private String appKey = null;
    private String identity;
    
    private String consumerKey = null;
    private String consumerSecret = null;
    private String accessToken = null;
    private String accessTokenSecret = null;
    
    
    
    private TwitterFactory twitterFactory;
    
    private GolgiTransportOptions stdGto;
    private GolgiTransportOptions hourGto;
    private GolgiTransportOptions dayGto;
    
    private class TweetHash extends Hashtable<String,String>{
    	
    }
    
    private class TweetFilterWithState extends TweetFilter{
    	long nextQuery;
    	long delay;
    	boolean enabled;
    }
    
    private Hashtable<String,TweetFilterWithState> filterHash = new Hashtable<String,TweetFilterWithState>();
    private Hashtable<String,TweetHash> tweetHashHash = new Hashtable<String,TweetHash>();
    private Hashtable<String,byte[]> imgHash = new Hashtable<String,byte[]>();
    
    
    private class NewTweetResultReceiver implements newTweet.ResultReceiver{
    	private String target;
        @Override
        public void success() {
            System.out.println("Sending newTweet: success");
        }
        
        @Override
        public void failure(GolgiException ex) {
            System.out.println("Sending newTweet fail: '" + ex.getErrText() + "'");
        }
        
        NewTweetResultReceiver(String target){
        	this.target = target;
        }
    }
    
    void sendTweet(String golgiId, TweetDetails tweetDetails){
    	newTweet.sendTo(
    			new NewTweetResultReceiver(golgiId),
    			hourGto,
    			golgiId,
    			tweetDetails);
    }
    
    private void loadFilters(){
    	try{
            BufferedReader br = new BufferedReader(new FileReader("TW-Filters.txt"));
            Hashtable<String,TweetFilterWithState> fHash = new Hashtable<String,TweetFilterWithState>();
            TweetFilterWithState tf;
            
            String str, id, query;
            int idx;
            while((str = br.readLine()) != null){
            	str = str.trim();
            	if((idx = str.indexOf(' ')) > 0){
            		id = str.substring(0, idx).trim();
            		query = str.substring(idx).trim();
            		tf = new TweetFilterWithState();
            		tf.setGolgiId(id);
            		tf.setQuery(query);
            		tf.delay = 30000;
            		tf.nextQuery = System.currentTimeMillis();
            		tf.enabled = true;
            		fHash.put(id, tf);
            		tweetHashHash.put(id,  new TweetHash());
            	}
            }
            System.out.println("Loaded " + fHash.size() + " users");
        	filterHash = fHash;
        }
        catch(IOException iex){
        }
    }
    
    private void saveFilters(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("TW-Filters.txt"));
            StringBuffer sb = new StringBuffer();
            for(Enumeration<TweetFilterWithState> e = filterHash.elements(); e.hasMoreElements();){
            	TweetFilterWithState f = e.nextElement();
            	if(f.enabled){
            		sb.append(f.getGolgiId() + " " + f.getQuery() + "\n");
            	}
            }
            bw.write(sb.toString());
            bw.close();
        }
        catch(IOException ioex){
        }
    }
    
    private startStreaming.RequestReceiver inboundStartStreaming = new startStreaming.RequestReceiver(){
        public void receiveFrom(startStreaming.ResultSender resultSender, TweetFilter tweetFilter){
        	boolean replaced = false;
            System.out.println("Asked to add: " + tweetFilter.getGolgiId() + " : '" + tweetFilter.getQuery() + "'");
            synchronized(filterHash){
            	TweetFilterWithState f1 = new TweetFilterWithState();
            	TweetFilterWithState f2;
            	f1.setGolgiId(tweetFilter.getGolgiId());
            	f1.setQuery(tweetFilter.getQuery());
            	f1.delay = 30000;
            	f1.nextQuery = System.currentTimeMillis();
            	f1.enabled = true;
            	f2 = filterHash.get(tweetFilter.getGolgiId());
            	
            	if(f2 == null || f2.getQuery().compareTo(f1.getQuery()) != 0){
            		filterHash.remove(tweetFilter.getGolgiId());            		
                	filterHash.put(tweetFilter.getGolgiId(), f1);
                	saveFilters();
                	replaced = true;
            	}
            	else{
            		System.out.println("Simply a re-enable");
            		f2.enabled = true;
            	}
            }
            if(replaced){
            	synchronized(tweetHashHash){
            		tweetHashHash.remove(tweetFilter.getGolgiId());
            		tweetHashHash.put(tweetFilter.getGolgiId(),  new TweetHash());
            	}
            }
            resultSender.success();
        }
    };
    
    private stopStreaming.RequestReceiver inboundStopStreaming = new stopStreaming.RequestReceiver(){
        public void receiveFrom(stopStreaming.ResultSender resultSender, String golgiId){
            System.out.println("Asked to stop: " + golgiId);
            synchronized(filterHash){
            	TweetFilterWithState f = filterHash.get(golgiId);
            	if(f != null){
            		f.enabled = false;
            	}
            	saveFilters();
            }
            resultSender.success();
        }
    };
    

    @Override
    public void registerSuccess() {
        System.out.println("Registered successfully with Golgi API");
    }


    @Override
    public void registerFailure() {
        System.err.println("Failed to register with Golgi API");
        System.exit(-1);
    }

    static void abort(String err) {
        System.err.println("Error: " + err);
        System.exit(-1);
    }

    
    
    private class TweetPump{
    	private Vector<Status> tweets;
    	private String target;
    	
    	private byte[] retrieveImage(String url){
            String result = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response;
            byte[] imgData = new byte[0];
            try{
            	response = httpclient.execute(httpget);
                                            
            	HttpEntity entity = response.getEntity();
            	if (entity != null) {
            		InputStream iStream = entity.getContent();
            		imgData = IOUtils.toByteArray(iStream);
            	}
            }
            catch(ClientProtocolException cpe){
            }
            catch(NoSuchElementException nse){
                
            }
            catch(IOException ioe){
            }
            
            return imgData;
    	}
    	
    	private void crank(){
    		while(tweets.size() > 0){
    			Status status = tweets.firstElement();
    			String suffix = "";
				String imgKey = status.getUser().getMiniProfileImageURL();
				byte[] imgData;
    			synchronized(imgHash){
    				if(!imgHash.containsKey(imgKey)){
    					imgHash.put(imgKey, retrieveImage(imgKey));
    					suffix = "*";
    				}
    				imgData = imgHash.get(imgKey);
    			}
    			System.out.println("" + status.getUser().getName() + ": image is " + imgData.length + suffix);

    			TweetDetails deets = new TweetDetails();
    					
    			deets.setUsername(status.getUser().getScreenName());
    			deets.setName(status.getUser().getName());
    			deets.setImage(imgHash.get(imgKey));
    			deets.setTimestamp((int)(status.getCreatedAt().getTime()/1000));
    			deets.setText(status.getText());
    			
    			newTweet.sendTo(new newTweet.ResultReceiver(){
    				@Override
    				public void success() {
    					System.out.println("newTweet() success");
    				}

    				@Override
    				public void failure(GolgiException ex) {
    					System.out.println("newTweet() fail: " + ex.getErrText());
    				}
    			}, hourGto, target, deets);
    			tweets.remove(0);
    		}
		}
    	
    	TweetPump(String target, Vector<Status> tweets){
    		this.target = target;
    		this.tweets = tweets;
    		
    		crank();
    	}
    }

    private int hkTmp = -1;
    private long lastQuery = 0;
    
    private char[] spinner = {'|', '/', '-', '\\'};
    private int spidx = 0;

    private void houseKeep(){
    	int total, enabled, due;
    	total = 0;
    	enabled = 0;
    	due = 0;
    	if((System.currentTimeMillis() - lastQuery) >= 10000){
    		Twitter twitter = twitterFactory.getInstance();
    		ArrayList<TweetFilterWithState> filterList = new ArrayList<TweetFilterWithState>();
    		synchronized(filterHash){
    			for(Enumeration<TweetFilterWithState> e = filterHash.elements(); e.hasMoreElements();){
    				total++;
    				TweetFilterWithState f1 = e.nextElement();
    				if(f1.enabled){
    					enabled++;
    					if(f1.nextQuery <= System.currentTimeMillis()){
    						due++;
    						filterList.add(f1);
    					}
    				}
    			}
    		}
        	System.out.print(spinner[spidx++ % spinner.length] + " " + due + "/" + enabled + "/" + total + "     \r");
        	System.out.flush();
        	
    		

			Collections.sort(filterList, new Comparator<TweetFilterWithState>(){
				@Override
				public int compare(TweetFilterWithState f1, TweetFilterWithState f2) {
					return (int)(f1.nextQuery - f2.nextQuery);
				}
			});

    		if(filterList.size() > 0){
    			System.out.println("There are " + filterList.size() + " ready-to-go filters");
				TweetFilterWithState f = filterList.remove(0);
    			
				Vector<Status> statusList = new Vector<Status>();
				//Query query = new Query("from:MountainViewPD OR from:LosAltosPD");
				lastQuery = System.currentTimeMillis();
				String str = f.getQuery().trim();
				if(str.length() > 0){
					Query query = new Query(f.getQuery().trim());
					try{
						QueryResult result = twitter.search(query);
						TweetHash tweetHash;
						synchronized(tweetHashHash){
			            	tweetHash = tweetHashHash.get(f.getGolgiId());
						}
						
						for (Status status : result.getTweets()) {
							String key = "" + status.getId();
							if(!tweetHash.containsKey(key)){
								tweetHash.put(key,  "");
								statusList.add(status);
							}
						}
					}
					catch(TwitterException twex){
						System.out.println("Twitter Explode: '" + twex.toString() + "'");
					}
					Collections.sort(statusList, new Comparator<Status>(){
						@Override
						public int compare(Status o1, Status o2) {
							return (int)(o1.getCreatedAt().getTime() - o2.getCreatedAt().getTime());
						}
					});

					if(statusList.size() <= 0){
						//
						// No results, push out query delay to a maximum of 5 minutes
						//
						
						f.delay = (f.delay * 15)/10;
						if(f.delay > 300000){
							f.delay = 300000;
						}
						System.out.println("Pushing out '" + f.getQuery() + "' to " + f.delay/1000 + " seconds");
					}
					else{
						//
						// Results, so increase the polling frequency to max
						//
						if(f.delay > 30000){
							f.delay = 30000;
							System.out.println("Pulling back '" + f.getQuery() + "' to " + f.delay/1000 + " seconds");
						}
						
						new TweetPump(f.getGolgiId(), statusList);
						
					}
    			}
				f.nextQuery = System.currentTimeMillis() + f.delay;
    		}
    	}
    }
    
    
    private void looper(){
        Class<GolgiAPI> apiRef = GolgiAPI.class;
        GolgiAPINetworkImpl impl = new GolgiAPINetworkImpl();
        GolgiAPI.setAPIImpl(impl);
        stdGto = new GolgiTransportOptions();
        stdGto.setValidityPeriod(60);

        hourGto = new GolgiTransportOptions();
        hourGto.setValidityPeriod(3600);

        dayGto = new GolgiTransportOptions();
        dayGto.setValidityPeriod(86400);

        loadFilters();
        
        startStreaming.registerReceiver(inboundStartStreaming);
        stopStreaming.registerReceiver(inboundStopStreaming);

        // WhozinService.registerDevice.registerReceiver(registerDeviceSS);
        GolgiAPI.register(devKey,
                          appKey,
                          "SERVER",
                          this);
        
        
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey(consumerKey)
          .setOAuthConsumerSecret(consumerSecret)
          .setOAuthAccessToken(accessToken)
          .setOAuthAccessTokenSecret(accessTokenSecret);
        twitterFactory = new TwitterFactory(cb.build());
        
        Timer hkTimer;
        hkTimer = new Timer();
        hkTimer.schedule(new TimerTask(){
            @Override
            public void run() {
                houseKeep();
            }
        }, 5000, 10000);
        
    }
    
    private Server(String[] args){
        for(int i = 0; i < args.length; i++){
        	if(args[i].compareTo("-devKey") == 0){
        		devKey = args[i+1];
        		i++;
        	}
        	else if(args[i].compareTo("-appKey") == 0){
        		appKey = args[i+1];
        		i++;
        	}
        	else if(args[i].compareTo("-consumer_key") == 0){
        		consumerKey = args[i+1];
        		i++;
        	}
        	else if(args[i].compareTo("-consumer_secret") == 0){
        		consumerSecret = args[i+1];
        		i++;
        	}
        	else if(args[i].compareTo("-access_token") == 0){
        		accessToken = args[i+1];
        		i++;
        	}
        	else if(args[i].compareTo("-access_token_secret") == 0){
        		accessTokenSecret = args[i+1];
        		i++;
        	}
        	else{
        		System.err.println("Zoikes, unrecognised option '" + args[i] + "'");
        		System.exit(-1);;
        	}
        }
        if(devKey == null){
        	System.out.println("No -devKey specified");
        	System.exit(-1);
        }
        else if(appKey == null){
        	System.out.println("No -appKey specified");
        	System.exit(-1);
        }
        else if(consumerKey == null){
        	System.out.println("No -consumer_key specified");
        	System.exit(-1);
        }
        else if(consumerSecret == null){
        	System.out.println("No -consumer_secret specified");
        	System.exit(-1);
        }
        else if(accessToken == null){
        	System.out.println("No -access_token specified");
        	System.exit(-1);
        }
        else if(accessTokenSecret == null){
        	System.out.println("No -access_token_secret specified");
        	System.exit(-1);
        }
    }
        
    public static void main(String[] args) {
        (new Server(args)).looper();
    }
}
