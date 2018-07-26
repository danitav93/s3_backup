package com.nodelab.s3;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.toedter.calendar.JDateChooser;

public class Backup_s3 {

	private JFrame frame;

	JDateChooser dateChooserDa;

	JDateChooser dateChooserA;

	JProgressBar pbProgress;

	JLabel lblStatus;

	JFilePicker filePicker;

	private S3 s3;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					Backup_s3 window = new Backup_s3();
					window.frame.setVisible(true);
					window.initializeS3();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Backup_s3() {

		initializeWindow();

	}

	/**
	 * 	Initialize s3 properties
	 */
	public void initializeS3() {

		final JDialog dlgProgress = new JDialog(frame, "Please wait...", true);//true means that the dialog created is modal
		lblStatus = new JLabel("Sto controllando l'accesso all'account di amazon s3..."); // this is just a label in which you can indicate the state of the processing

		pbProgress = new JProgressBar(0, 100);
		pbProgress.setIndeterminate(true); //we'll use an indeterminate progress bar
		dlgProgress.add(BorderLayout.NORTH, lblStatus);
		dlgProgress.add(BorderLayout.CENTER, pbProgress);
		dlgProgress.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // prevent the user from closing the dialog
		dlgProgress.setSize(400, 90);
		dlgProgress.setLocationRelativeTo(frame);
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					s3=new S3();
					Thread.sleep(2000);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), 
							"Attenzione", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				return null;
			}
			@Override
			protected void done() {
				dlgProgress.dispose();//close the modal dialog
			}
		};

		sw.execute(); // this will start the processing on a separate thread
		dlgProgress.setVisible(true); //this will block user input as long as the processing task is working


	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeWindow() {

		frame = new JFrame();
		frame.setBounds(100, 100, 696, 309);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		JLabel lblA = new JLabel("a");
		lblA.setBounds(177, 49, 24, 14);
		frame.getContentPane().add(lblA);

		filePicker = new JFilePicker("Scegli la cartella di destinazione", "Browse...");
		filePicker.setBounds(-14, 87, 660, 33);
		frame.getContentPane().add(filePicker);
		filePicker.setMode(JFilePicker.MODE_OPEN);

		JButton btnEsegui = new JButton("Esegui");

		btnEsegui.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//controllo di aver inserito tutti i campi
				if (dateChooserDa.getDate()==null || dateChooserA.getDate()==null || filePicker.getSelectedFilePath().length()==0) {
					JOptionPane.showMessageDialog(frame, "Tutti i campi devono essere valorizzati!", 
							"Attenzione", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//controllo che la data "da" sia precedente alla data "a"
				if (! (dateChooserDa.getDate().before(dateChooserA.getDate()) || dateChooserDa.getDate().equals(dateChooserA.getDate()))) {
					JOptionPane.showMessageDialog(frame, "Inserire le date in ordine coerente!", 
							"Attenzione", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				final JDialog dlgProgress = new JDialog(frame, "Please wait...", true);//true means that the dialog created is modal
				lblStatus = new JLabel("Working..."); // this is just a label in which you can indicate the state of the processing

				pbProgress = new JProgressBar(0, 100);
				//pbProgress.setIndeterminate(true); //we'll use an indeterminate progress bar
				dlgProgress.add(BorderLayout.NORTH, lblStatus);
				dlgProgress.add(BorderLayout.CENTER, pbProgress);
				dlgProgress.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // prevent the user from closing the dialog
				dlgProgress.setSize(300, 90);
				dlgProgress.setLocationRelativeTo(frame);

				SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						executeBackup();
						return null;
					}

					@Override
					protected void done() {
						dlgProgress.dispose();//close the modal dialog
					}
				};

				sw.execute(); // this will start the processing on a separate thread
				dlgProgress.setVisible(true); //this will block user input as long as the processing task is working
			}
		});
		btnEsegui.setBounds(10, 154, 108, 23);
		frame.getContentPane().add(btnEsegui);

		JLabel lblBackup = new JLabel("Backup per data");
		lblBackup.setBounds(10, 11, 108, 14);
		frame.getContentPane().add(lblBackup);



		JLabel lblDa = new JLabel("da");
		lblDa.setBounds(10, 46, 24, 20);
		frame.getContentPane().add(lblDa);

		dateChooserDa = new JDateChooser();
		dateChooserDa.setBounds(29, 44, 112, 20);
		frame.getContentPane().add(dateChooserDa);
		dateChooserDa.setAlignmentY(Component.BOTTOM_ALIGNMENT);

		dateChooserA = new JDateChooser();
		dateChooserA.setBounds(194, 44, 112, 20);
		frame.getContentPane().add(dateChooserA);

	}

	private void executeBackup() {
		try {
			s3.executeBackup(pbProgress,lblStatus,dateChooserDa.getDate(),dateChooserA.getDate(),filePicker.getSelectedFilePath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, e.getMessage(), 
					"Attenzione", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

}
