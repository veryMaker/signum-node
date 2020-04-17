package brs;

import brs.props.PropertyService;
import brs.props.Props;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.Permission;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class BurstGUI extends JFrame {
    private static final String ICON_LOCATION = "/images/burst_overlay_logo.png";
    private static final String FAILED_TO_START_MESSAGE = "BurstGUI caught exception starting BRS";
    private static final String UNEXPECTED_EXIT_MESSAGE = "BRS Quit unexpectedly! Exit code ";

    private static final int OUTPUT_MAX_LINES = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(BurstGUI.class);
    private static String[] args;

    private boolean userClosed = false;
    private TrayIcon trayIcon = null;
    private JToolBar toolBar = null;

    public static void main(String[] args) {
        BurstGUI.args = args;
        new BurstGUI();
    }

    public BurstGUI() {
        System.setSecurityManager(new BurstGUISecurityManager());
        setTitle("Burst Reference Software version " + Burst.VERSION);
        JTextArea textArea = new JTextArea() {
			@Override
			public void replaceRange(String str, int start, int end) {
				super.replaceRange(str, start, end);
                while(getText().split("\n", -1).length > OUTPUT_MAX_LINES) {
                    int fle = getText().indexOf('\n');
                    super.replaceRange("", 0, fle+1);
                }
                setCaretPosition(getText().length());
            }
        };
        textArea.setEditable(false);
        sendJavaOutputToTextArea(textArea);
        JScrollPane pane = new JScrollPane(textArea);
        JPanel content = new JPanel(new BorderLayout());
        setContentPane(content);
        content.add(pane, BorderLayout.CENTER);
        
        toolBar = new JToolBar();
        content.add(toolBar, BorderLayout.PAGE_START);
        
        setSize(800, 450);
        setLocationRelativeTo(null);
        try {
			setIconImage(ImageIO.read(getClass().getResourceAsStream(ICON_LOCATION)));
		} catch (IOException e) {
			e.printStackTrace();
		}
        new Thread(this::runBrs).start();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		if(trayIcon == null) {
        			if (JOptionPane.showConfirmDialog(BurstGUI.this, 
        					"This will stop BRS. Are you sure?", "Exit and stop BRS", 
        					JOptionPane.YES_NO_OPTION,
        					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
        				shutdown();
        			}
        		}
        		else
        			setVisible(false);
        	}
        });
        
		IconFontSwing.register(FontAwesome.getIconFont());
        SwingUtilities.invokeLater(() -> showTrayIcon());
    }

    private void shutdown() {
        userClosed = true;
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        System.exit(0); // BRS shutdown handled by exit hook
    }

    private void showTrayIcon() {
        if (trayIcon == null) { // Don't start running in tray twice
            trayIcon = createTrayIcon();
            if (trayIcon == null) {
            	// No tray icon available, so show the window
                showWindow();
            }
        }
    }

    private TrayIcon createTrayIcon() {
    	PopupMenu popupMenu = new PopupMenu();
    	
    	MenuItem openWebUiItem = new MenuItem("Open Web Interface");
    	MenuItem showItem = new MenuItem("Show BRS output");
    	MenuItem shutdownItem = new MenuItem("Shutdown BRS");

    	JButton openWebUiButton = new JButton(openWebUiItem.getLabel(), IconFontSwing.buildIcon(FontAwesome.WINDOW_RESTORE, 18));
    	JButton editConfButton = new JButton("Edit conf file", IconFontSwing.buildIcon(FontAwesome.PENCIL, 18));
    	JButton popOffButton = new JButton("Pop off 100 blocks", IconFontSwing.buildIcon(FontAwesome.BACKWARD, 18));
    	
    	openWebUiButton.addActionListener(e -> openWebUi());
    	editConfButton.addActionListener(e -> editConf());
    	popOffButton.addActionListener(e -> popOff());
 
    	toolBar.add(openWebUiButton);
    	toolBar.add(editConfButton);
    	toolBar.add(popOffButton);

    	openWebUiItem.addActionListener(e -> openWebUi());
    	showItem.addActionListener(e -> showWindow());
    	shutdownItem.addActionListener(e -> shutdown());

    	popupMenu.add(openWebUiItem);
    	popupMenu.add(showItem);
    	popupMenu.add(shutdownItem);

    	try {
    		TrayIcon newTrayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(BurstGUI.class.getResource(ICON_LOCATION)), "Burst Reference Software", popupMenu);
    		newTrayIcon.setImage(newTrayIcon.getImage().getScaledInstance(newTrayIcon.getSize().width, -1, Image.SCALE_SMOOTH));
    		newTrayIcon.addActionListener(e -> openWebUi());

    		SystemTray systemTray = SystemTray.getSystemTray();
    		systemTray.add(newTrayIcon);
    		return newTrayIcon;
    	} catch (Exception e) {
    		LOGGER.warn("Could not create tray icon");
    		return null;
    	}
    }

    private void showWindow() {
    	setVisible(true);
    }
    
    private void popOff() {
    	Block lastBlock = Burst.getBlockchain().getLastBlock();
    	LOGGER.info("Pop off requested, this can take a while...");
    	new Thread(() -> Burst.getBlockchainProcessor().popOffTo(lastBlock.getHeight() - 100)).start();
    }
    
    private void editConf() {
    	URL configFile = ClassLoader.getSystemResource(Burst.PROPERTIES_NAME);
    	File file = new File(configFile.getFile());
    	if(!file.exists()) {
        	configFile = ClassLoader.getSystemResource(Burst.DEFAULT_PROPERTIES_NAME);
        	file = new File(configFile.getFile());
        	if(!file.exists()) {
        		file = new File(Burst.DEFAULT_PROPERTIES_NAME);
        	}
    	}
    	
    	if(!file.exists()) {
    		JOptionPane.showMessageDialog(this, "Could not find conf file: " + configFile, "File not found", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			LOGGER.error("Could not edit conf file", e);
		}
    }

    private void openWebUi() {
        try {
            PropertyService propertyService = Burst.getPropertyService();
            int port = propertyService.getBoolean(Props.DEV_TESTNET) ? propertyService.getInt(Props.DEV_API_PORT) : propertyService.getInt(Props.API_PORT);
            String httpPrefix = propertyService.getBoolean(Props.API_SSL) ? "https://" : "http://";
            String address = httpPrefix + "localhost:" + port;
            try {
                Desktop.getDesktop().browse(new URI(address));
            } catch (Exception e) { // Catches parse exception or exception when opening browser
                LOGGER.error("Could not open browser", e);
                showMessage("Error opening web UI. Please open your browser and navigate to " + address);
            }
        } catch (Exception e) { // Catches error accessing PropertyService
            LOGGER.error("Could not access PropertyService", e);
            showMessage("Could not open web UI as could not read BRS configuration.");
        }
    }

    private void runBrs() {
        try {
            Burst.main(args);
            try {
                if (Burst.getPropertyService().getBoolean(Props.DEV_TESTNET)) {
                    onTestNetEnabled();
                }
            } catch (Exception t) {
                LOGGER.error("Could not determine if running in testnet mode", t);
            }
        } catch (SecurityException ignored) {
        } catch (Exception t) {
            LOGGER.error(FAILED_TO_START_MESSAGE, t);
            showMessage(FAILED_TO_START_MESSAGE);
            onBrsStopped();
        }
    }

    private void onTestNetEnabled() {
        SwingUtilities.invokeLater(() -> setTitle(getTitle() + " (TESTNET)"));
        if(trayIcon != null)
        	trayIcon.setToolTip(trayIcon.getToolTip() + " (TESTNET)");
    }

    private void onBrsStopped() {
        SwingUtilities.invokeLater(() -> setTitle(getTitle() + " (STOPPED)"));
        if(trayIcon != null)
        	trayIcon.setToolTip(trayIcon.getToolTip() + " (STOPPED)");
    }

    private void sendJavaOutputToTextArea(JTextArea textArea) {
        System.setOut(new PrintStream(new TextAreaOutputStream(textArea, System.out)));
        System.setErr(new PrintStream(new TextAreaOutputStream(textArea, System.err)));
    }

    private void showMessage(String message) {
    	SwingUtilities.invokeLater(() -> {
            System.err.println("Showing message: " + message);
            JOptionPane.showMessageDialog(this, message, "BRS Message", JOptionPane.ERROR_MESSAGE);
        });
    }

    private static class TextAreaOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final PrintStream actualOutput;

        private StringBuilder lineBuilder = new StringBuilder();

        private TextAreaOutputStream(JTextArea textArea, PrintStream actualOutput) {
            this.textArea = textArea;
            this.actualOutput = actualOutput;
        }

        @Override
        public void write(int b) {
            writeString(new String(new byte[]{(byte)b}));
        }

        @Override
        public void write(byte[] b) {
            writeString(new String(b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            writeString(new String(b, off, len));
        }

        private void writeString(String string) {
            lineBuilder.append(string);
            String line = lineBuilder.toString();
            if (line.contains("\n")) {
                actualOutput.print(line);
                if (textArea != null)
                	SwingUtilities.invokeLater(() -> textArea.append(line));
                lineBuilder.delete(0, lineBuilder.length());
            }
        }
    }

    private class BurstGUISecurityManager extends SecurityManager {

        @Override
        public void checkExit(int status) {
            if (!userClosed && status != 0) {
                LOGGER.error("{} {}", UNEXPECTED_EXIT_MESSAGE, status);
                SwingUtilities.invokeLater(() -> showWindow());
                showMessage(UNEXPECTED_EXIT_MESSAGE + String.valueOf(status));
                onBrsStopped();
                throw new SecurityException();
            }
        }

        @Override
        public void checkPermission(Permission perm) {
            // No need to check.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // No need to check.
        }

        @Override
        public void checkCreateClassLoader() {
            // No need to check.
        }

        @Override
        public void checkAccess(Thread t) {
            // No need to check.
        }

        @Override
        public void checkAccess(ThreadGroup g) {
            // No need to check.
        }

        @Override
        public void checkExec(String cmd) {
            // No need to check.
        }

        @Override
        public void checkLink(String lib) {
            // No need to check.
        }

        @Override
        public void checkRead(FileDescriptor fd) {
            // No need to check.
        }

        @Override
        public void checkRead(String file) {
            // No need to check.
        }

        @Override
        public void checkRead(String file, Object context) {
            // No need to check.
        }

        @Override
        public void checkWrite(FileDescriptor fd) {
            // No need to check.
        }

        @Override
        public void checkWrite(String file) {
            // No need to check.
        }

        @Override
        public void checkDelete(String file) {
            // No need to check.
        }

        @Override
        public void checkConnect(String host, int port) {
            // No need to check.
        }

        @Override
        public void checkConnect(String host, int port, Object context) {
            // No need to check.
        }

        @Override
        public void checkListen(int port) {
            // No need to check.
        }

        @Override
        public void checkAccept(String host, int port) {
            // No need to check.
        }

        @Override
        public void checkMulticast(InetAddress maddr) {
            // No need to check.
        }

        @Override
        public void checkPropertiesAccess() {
            // No need to check.
        }

        @Override
        public void checkPropertyAccess(String key) {
            // No need to check.
        }

        @Override
        public void checkPrintJobAccess() {
            // No need to check.
        }

        @Override
        public void checkPackageAccess(String pkg) {
            // No need to check.
        }

        @Override
        public void checkPackageDefinition(String pkg) {
            // No need to check.
        }

        @Override
        public void checkSetFactory() {
            // No need to check.
        }

        @Override
        public void checkSecurityAccess(String target) {
            // No need to check.
        }
    }
}
