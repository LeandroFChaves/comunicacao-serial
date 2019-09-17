package br.com.pertocheck.lyceum;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class PchekComm implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean portaAberta;
	private SerialPort porta;
	private OutputStream out;
	private InputStream in;

	private final long TIMEOUT_DSR = 1000L;
	private final long TIMEOUT_ACK = 500L;

	private final byte ACK = 6;
	private final byte NAK = 21;
	private final int MAX_TENT = 3;

	private final int ERRO_OPEN = 1000;
	private final int ERRO_WRITE = 1001;
	private final int ERRO_READ = 1002;
	private final int ERRO_NAK = 1003;
	private final int ERRO_TIMEOUT = 1004;
	private final int ERRO_DSR = 1005;
	private final int ERRO_PROTOCOLO = 1006;
	private final int ERRO_TIMEOUT_ACK = 1007;

	private final String ERRO_OPEN_STR = "Erro ao abrir a porta!";
	private final String ERRO_WRITE_STR = "Erro ao escrever na porta!";
	private final String ERRO_READ_STR = "Erro ao ler da porta!";
	private final String ERRO_NAK_STR = "NAK recebido! Tentativas esgotadas!";
	private final String ERRO_TIMEOUT_STR = "Tempo esgotado esperando resposta!";
	private final String ERRO_EQP_DESL_OCP = "Equipamento ocupado/desligado!";
	private final String ERRO_PROTOCOLO_STR = "Erro no protocolo! Tentativas esgotadas!";
	private final String ERRO_TIMEOUT_ACK_STR = "Tempo esgotado esperando ACK!";

	private String resposta;
	private String erroStr = "";
	private int erroInt = 0;

	public PchekComm(String porta, int taxaTransmissao, int timeout) {
		try {
			this.abrirPorta(porta, taxaTransmissao, timeout);
		} catch (Exception e) {
			e.printStackTrace();

			return;
		}
	}

	public boolean processarComando(String comando, int paramInt) {
		if (!this.portaAberta) {
			return false;
		}

		if (!checaDSR(TIMEOUT_DSR)) {
			return false;
		}

		if (!enviarComando(comando)) {
			return false;
		}

		if (!receberResposta(paramInt)) {
			return false;
		}

		return true;
	}

	public String getResposta() {
		return this.resposta;
	}

	public String getErro() {
		return this.erroInt + " - " + this.erroStr;
	}
	
	public boolean isComunicacao() {
		return this.portaAberta;
	}

	public void terminate() {
		if (this.portaAberta) {
			this.porta.close();
		}
	}

	private void abrirPorta(String porta, int taxaTransmissao, int timeout) {
		try {
			CommPortIdentifier idPorta = this.obterIdPorta(porta);
			this.porta = (SerialPort) idPorta.open("PchekComm", timeout);

			this.in = this.porta.getInputStream();
			this.out = this.porta.getOutputStream();

			this.porta.setSerialPortParams(taxaTransmissao, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			this.porta.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			this.portaAberta = true;
		} catch (Exception e) {
			this.portaAberta = false;
			this.erroInt = ERRO_OPEN;
			this.erroStr = ERRO_OPEN_STR;

			System.out.println("Erro abrindo comunicação: " + e);
			System.exit(1);
		}
	}

	private CommPortIdentifier obterIdPorta(String porta) {
		CommPortIdentifier localCommPortIdentifier = null;

		try {
			localCommPortIdentifier = CommPortIdentifier.getPortIdentifier(porta);

			if (localCommPortIdentifier == null) {
				System.out.println("Erro na porta");

				System.exit(1);
			}
		} catch (Exception e) {
			System.out.println("Erro obtendo ID da porta: " + e);

			System.exit(1);
		}

		return localCommPortIdentifier;
	}

	private boolean checaDSR(long paramLong) {
		long l = System.currentTimeMillis();

		do {
			if (System.currentTimeMillis() > l + paramLong) {
				this.erroInt = ERRO_DSR;
				this.erroStr = ERRO_EQP_DESL_OCP;
				return false;
			}
		} while (!this.porta.isDSR());

		return true;
	}

	private int getByte() {
		int i = -1;

		try {
			if (this.in.available() > 0) {
				i = this.in.read();
			}
		} catch (Exception localException) {
			this.erroInt = ERRO_READ;
			this.erroStr = ERRO_READ_STR;

			return -2;
		}

		return i;
	}

	private boolean enviarComando(final String s) {
		final Protocolo protocolo = new Protocolo();
		protocolo.prepararComando(s);

		final long currentTimeMillis = System.currentTimeMillis();

		int n = 0;
		while (true) {
			try {
				this.out.write(protocolo.getBuffer());
			} catch (Exception ex) {
				this.erroInt = ERRO_WRITE;
				this.erroStr = ERRO_WRITE_STR;

				return false;
			}

			int byte1;
			do {
				byte1 = this.getByte();
				if (byte1 == -2) {
					return false;
				}

				if (currentTimeMillis + TIMEOUT_ACK < System.currentTimeMillis()) {
					this.erroInt = ERRO_TIMEOUT_ACK;
					this.erroStr = ERRO_TIMEOUT_ACK_STR;

					return false;
				}

			} while (byte1 != 6 && byte1 != 21);

			if (byte1 == 21) {
				System.out.println("NAK!");
			}

			if (byte1 == 6 || ++n >= MAX_TENT) {
				if (byte1 == 21) {
					this.erroInt = ERRO_NAK;
					this.erroStr = ERRO_NAK_STR;

					return false;
				}
				return true;
			}
		}
	}

	private boolean receberResposta(int paramInt) {
		Protocolo localProtocolo = new Protocolo(1024);

		long l1 = System.currentTimeMillis();
		long l2 = paramInt * 1000;
		int j = 0;

		do {
			boolean bool = false;

			do {
				int i = getByte();
				
				if (i == -2) {
					return false;
				}
				
				if (l1 + l2 < System.currentTimeMillis()) {
					this.erroInt = ERRO_TIMEOUT;
					this.erroStr = ERRO_TIMEOUT_STR;
					
					return false;
				}
				
				if (i >= 0) {
					bool = localProtocolo.acrescentarByte((byte) i);
				}
			} while (!bool);

			if (localProtocolo.getRecepcaoOk()) {
				enviarAck();
				this.resposta = localProtocolo.getResposta();

				return true;
			}
			enviarNak();

			localProtocolo.iniciarRecepcao();

			j++;
		} while (j < MAX_TENT);

		this.erroInt = ERRO_PROTOCOLO;
		this.erroStr = ERRO_PROTOCOLO_STR;

		return false;
	}

	private void enviarAck() {
		try {
			this.out.write(ACK);
		} catch (Exception localException) {
			System.out.println(localException);
		}
	}

	private void enviarNak() {
		try {
			this.out.write(NAK);
		} catch (Exception localException) {
			System.out.println(localException);
		}
	}
	
}