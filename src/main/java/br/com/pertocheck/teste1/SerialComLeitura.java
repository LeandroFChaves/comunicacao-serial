package br.com.pertocheck.teste1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialComLeitura implements Runnable, SerialPortEventListener {

	public String Dadoslidos;
	public int nodeBytes;
	private int baudrate;
	private int timeout;
	private CommPortIdentifier cp;
	private SerialPort porta;
	private OutputStream saida;
	private InputStream entrada;
	private Thread threadLeitura;
	private boolean IDPortaOK;
	private boolean PortaOK;
	private boolean Leitura;
	private boolean Escrita;
	private String Porta;
	protected String peso;

	private byte bcc;
	private byte[] buffer;

	public SerialComLeitura(String p, int b, int t) {
		this.Porta = p;
		this.baudrate = b;
		this.timeout = t;

	}

	public void setPeso(String peso) {
		this.peso = peso;
	}

	public String getPeso() {
		return peso;
	}

	public void HabilitarEscrita() {
		Escrita = true;
		Leitura = false;
	}

	public void HabilitarLeitura() {
		Escrita = false;
		Leitura = true;
	}

	public void ObterIdDaPorta() {
		try {
			cp = CommPortIdentifier.getPortIdentifier(Porta);

			if (cp == null) {
				System.out.println("Erro na porta");

				IDPortaOK = false;
				System.exit(1);
			}

			IDPortaOK = true;
		} catch (Exception e) {
			System.out.println("Erro obtendo ID da porta: " + e);

			IDPortaOK = false;
			System.exit(1);
		}
	}

	public void AbrirPorta() {
		try {
			porta = (SerialPort) cp.open("SerialComLeitura", timeout);
			PortaOK = true;

			// configurar parâmetros
			porta.setSerialPortParams(baudrate, porta.DATABITS_8, porta.STOPBITS_1, porta.PARITY_NONE);

			porta.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (Exception e) {
			PortaOK = false;
			System.out.println("Erro abrindo comunicação: " + e);
			System.exit(1);
		}
	}

	public void LerDados() {
		if (Escrita == false) {
			try {
				entrada = porta.getInputStream();

				System.out.println(entrada);
			} catch (Exception e) {
				System.out.println("Erro de stream: " + e);
				System.exit(1);
			}

			try {
				porta.addEventListener(this);
			} catch (Exception e) {
				System.out.println("Erro de listener: " + e);
				System.exit(1);
			}

			porta.notifyOnDataAvailable(true);

			try {
				threadLeitura = new Thread(this);
				threadLeitura.start();
				run();
			} catch (Exception e) {
				System.out.println("Erro de Thred: " + e);
			}
		}
	}

	public void EnviarUmaString(String msg) {
		if (Escrita == true) {
			try {
				saida = porta.getOutputStream();
				System.out.println("FLUXO OK!");
			} catch (Exception e) {
				System.out.println("Erro.STATUS: " + e);
			}

			try {
				System.out.println("Enviando um byte para " + Porta);
				System.out.println("Enviando : " + msg);

				// Pertocheck
				montaCmd(msg);

				saida.write(this.buffer);
				Thread.sleep(100);
				
				this.leitura();

				saida.flush();
			} catch (Exception e) {
				System.out.println("Houve um erro durante o envio. ");
				System.out.println("STATUS: " + e);
				System.exit(1);
			}
		} else {
			System.exit(1);
		}
	}

	public void montaCmd(String paramString) {
		int i = 0;
		int j = paramString.length();
		this.buffer = new byte[j + 3];

		this.buffer[(i++)] = 2;
		this.bcc = 2;
		for (int m = 0; m < j; m++) {
			int k = (byte) paramString.charAt(m);
			this.buffer[(i++)] = (byte) k;
			this.bcc = ((byte) (this.bcc ^ k));
		}
		this.buffer[(i++)] = 3;
		this.bcc = ((byte) (this.bcc ^ 0x3));
		this.buffer[(i++)] = this.bcc;
	}

	public void run() {
		try {
			Thread.sleep(5);
		} catch (Exception e) {
			System.out.println("Erro de Thred: " + e);
		}
	}

	public void serialEvent(SerialPortEvent ev) {
		StringBuffer bufferLeitura = new StringBuffer();

		int novoDado = 0;

		switch (ev.getEventType()) {

		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			// Novo algoritmo de leitura.
			while (novoDado != -1) {
				try {
					novoDado = entrada.read();

					System.out.println(novoDado);

					if (novoDado == -1) {
						break;
					}

					if ('\r' == (char) novoDado) {
						bufferLeitura.append('\n');
					} else {
						bufferLeitura.append((char) novoDado);
					}
				} catch (IOException ioe) {
					System.out.println("Erro de leitura serial: " + ioe);
				}
			}
			setPeso(new String(bufferLeitura));

			System.out.println(getPeso());
			break;
		}
	}

	public void FecharCom() {
		try {
			porta.close();
		} catch (Exception e) {
			System.out.println("Erro fechando porta: " + e);
			System.exit(0);
		}
	}

	public String obterPorta() {
		return Porta;
	}

	public int obterBaudrate() {
		return baudrate;
	}

	public void leitura() {
		this.HabilitarLeitura();
		this.LerDados();

		// Controle de tempo da leitura aberta na serial
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			System.out.println("Erro na Thread: " + ex);
		}

		this.FecharCom();
	}

}