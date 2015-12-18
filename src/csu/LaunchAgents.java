package csu;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;

import csu.Viewer.TestFBViewer;
import csu.Viewer.TestViewer;
import csu.model.AgentConstants;

import java.util.List;
import java.util.ArrayList;
import rescuecore2.Constants;
import rescuecore2.GUIComponent;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.components.ComponentInitialisationException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.registry.Registry;
import rescuecore2.log.Logger;

public final class LaunchAgents {

	private static final String NO_GUI_FLAG = "--nogui";
	private static final String PRECOMPUTE_FLAG = "-precompute";
	/**
	 * Pre_compute控制位，用于控制智能体初始化时是否使用pre_compute。这个值是由启动脚本决定的，当使用start.sh启动
	 * 智能体时，该值为false。当使用precompute.sh启动时，该值为true。
	 * <p>
	 * 所谓pre_compute，就是在智能体连接时，也就是代码中的初始化部分，对于一些计算量大且重复的操作，可以通过文件操作来
	 * 提高连接速度。具体就是，连接的时候，第一个智能体会会做所有的计算操作，然后把那些计算量大且重复的操作的结果写到特定
	 * 文件中去，接下来的智能体连接时，就可以直接从那个文件中读取计算结果，而不必再做那些重复的计算。
	 * <p>
	 * 注意：
	 * 文件操作只能在初始化的时候使用，也就是pre_compute。当仿真周期开始时，不允许存在任何的文件操作，也就是你的think
	 * 函数不允许出现任何文件操作。还有就是，pre_compute有可能也不被允许，所以在pre_compute不被允许的情况下，代码依旧
	 * 要能正常连接运行。
	 */
	public static boolean SHOULD_PRECOMPUTE = true;

	private LaunchAgents() {
		
	}

	public static void main(String[] args) {
		// Logger.setLogContext("launcher");
		Config config = new Config();
		boolean gui = true;
		try {
			// the hostname and random seed will be processed.
			args = CommandLineOptions.processArgs(args, config);  
			List<String> toLaunch = new ArrayList<String>();
			boolean precompute = false;   // flag to determines whether to use precompute
			for (String next : args) {
				if (NO_GUI_FLAG.equals(next)) {
					gui = false;
				} else if (PRECOMPUTE_FLAG.equals(next)) {
					precompute = true;
					SHOULD_PRECOMPUTE = precompute;
				} else {
					toLaunch.add(next);
				}
			}
			
			/*
			 * Handle the precompute case. If the precompute directory does not exist, then make it. 
			 * If the precompute directory exists, then delete all items generated in last simulation.
			 */
			if (precompute) {
				File data = new File("precompute");
				if (!data.exists() || !data.isDirectory()) {
					data.mkdir();	
				} else {
					for (File file : data.listFiles()) {
						if (!file.isDirectory())
							file.delete();
					}
				}
			}
			
			if (AgentConstants.PRINT_COMMUNICATION) {
				File data = new File("commOutput");
				if (!data.exists() || !data.isDirectory()) {
					data.mkdir();
				} else {
					for (File file : data.listFiles()) {
						if (!file.isDirectory())
							file.delete();
					}
				}
			}
			
			int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY,
					Constants.DEFAULT_KERNEL_PORT_NUMBER);
			String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY,
					Constants.DEFAULT_KERNEL_HOST_NAME);
			processJarFiles(config);

			ComponentLauncher launcher = new TCPComponentLauncher(host, port, config);
			for (String next : toLaunch) {
				connect(launcher, next, gui);
			}
			System.out.println("All agents was connected.");
			
			// test viewer part
			if (AgentConstants.LAUNCH_VIEWER) {
				try { 
					Logger.info("Connecting viewer ...");
				launcher.connect(new TestViewer());
				///launcher.connect(new TestFBViewer());
					Logger.info("success");
				} catch (ComponentConnectionException e) {
					Logger.info("failed: " + e.getMessage());
				}
			}
			
		} catch (IOException e) {
			Logger.error("Error connecting components", e);
		} catch (ConfigException e) {
			Logger.error("Configuration error", e);
		} catch (ConnectionException e) {
			Logger.error("Error connecting components", e);
		} catch (InterruptedException e) {
			Logger.error("Error connecting components", e);
		}
	}

	/**
	 * This method will register the Entity, Message and Property factory.
	 */
	private static void processJarFiles(Config config) throws IOException {
		LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
		processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
		processor.process();
	}

	private static void connect(ComponentLauncher launcher, String argLine, boolean gui) 
			throws InterruptedException, ConnectionException {
		// the class name of agent and the number of this agent was sperated by "*"
		int index = argLine.indexOf("*");
		int count = 1;						// the count of agent
		String className = argLine;			// the class name of the component to be launch

		if (index != -1) {
			String mult = argLine.substring(index + 1);
			if ("n".equals(mult)) {
				count = Integer.MAX_VALUE;
			} else {
				count = Integer.parseInt(mult);
			}
			className = argLine.substring(0, index);
		}
		System.out.println("Launching " + (count == Integer.MAX_VALUE ? "many" : count)
				+ " instances of component '" + className + "'...");
		for (int i = 0; i != count; ++i) {
			Component c = instantiate(className, Component.class);
			if (c == null) {
				break;
			}
			System.out.println("Launching instance " + (i + 1) + "...");
			try {
				c.initialise();
				launcher.connect(c);
				if (gui && c instanceof GUIComponent) {

					GUIComponent g = (GUIComponent) c;
					JFrame frame = new JFrame(g.getGUIComponentName());
					frame.setContentPane(g.getGUIComponent());
					frame.pack();
					frame.setVisible(true);
				}
				System.out.println("success");
			} catch (ComponentConnectionException e) {
				Logger.info("failed: " + e.getMessage());
				break;
			} catch (ComponentInitialisationException e) {
				Logger.info("failed: " + e);
			} catch (ConnectionException e) {
				Logger.info("failed: " + e);
				break;
			}
		}
	}
}
