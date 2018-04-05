package com.rdiot.polly;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class PollyDemo {

	private final AmazonPollyClient polly;
	private final Voice voice;

	private static final String TRUN_ON = "OK Google, turn the light on";
	private static final String TRUN_OFF = "OK Google, turn the light off";
	
	
	public PollyDemo(Region region) {
		// create an Amazon Polly client in a specific region
		polly = new AmazonPollyClient(new DefaultAWSCredentialsProviderChain(), 
		new ClientConfiguration());
		polly.setRegion(region);
		// Create describe voices request.
		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest().withLanguageCode("en-US");
		
		// Synchronously ask Amazon Polly to describe available TTS voices.
		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
	
		voice = describeVoicesResult.getVoices().get(0);
        
	}
	
	public static String roadLocalFile(String filepath) {
	    String readFile= "";
	    try {
	        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filepath),"UTF-8"));
	        String s;
	        while ((s = in.readLine()) != null) {
	            readFile+= s;
	        }
	        in.close();
	    } catch (IOException e) {
	        System.err.println(e);
	        System.exit(1);
	    }
	 
	    return readFile;
	}

	public InputStream synthesize(String text, OutputFormat format) throws IOException {
		SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId())
				.withOutputFormat(format);
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

		return synthRes.getAudioStream();
	}

	public static void main(String args[]) throws Exception {
	
		String OnOff = roadLocalFile("./command.txt");
		String Display;
		if (OnOff.contains("ON")) {
			Display = TRUN_ON;	
		}
		else 
			Display = TRUN_OFF;
		 			
		//create the test class
		PollyDemo helloWorld = new PollyDemo(Region.getRegion(Regions.AP_NORTHEAST_2));
		//get the audio stream
		InputStream speechStream = helloWorld.synthesize(Display, OutputFormat.Mp3);

		//create an MP3 player
		AdvancedPlayer player = new AdvancedPlayer(speechStream,
				javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

		player.setPlayBackListener(new PlaybackListener() {
			@Override
			public void playbackStarted(PlaybackEvent evt) {
				System.out.println("#################################################################");				
				System.out.println("P018 : RD IoT Amazon Polly + Google Home + Sonoff Wifi + RailLight");
				System.out.println("#################################################################");				
				System.out.println("Playback started");
				System.out.println("-----------------------------------------------------------------");				
				System.out.println("TTS : " + Display);
			}
			
			@Override
			public void playbackFinished(PlaybackEvent evt) {
				System.out.println("-----------------------------------------------------------------");				
				System.out.println("Playback finished");
				System.out.println("#################################################################");				
			}
		});
		
		// play it!
		player.play();
	
	}

}
