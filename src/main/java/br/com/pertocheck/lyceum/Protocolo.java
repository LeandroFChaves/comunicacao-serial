package br.com.pertocheck.lyceum;

public class Protocolo {
	private byte[] buffer;
	private int contador;
	private int passo;
	private boolean ok;
	private byte bcc;
	private int maximo;
	private final byte STX = 2;
	private final byte ETX = 3;

	public Protocolo() {
	}

	public Protocolo(int paramInt) {
		this.buffer = new byte[paramInt];
		this.maximo = paramInt;
		
		iniciarRecepcao();
	}

	public void prepararComando(String paramString) {
		int i = 0;
		int j = paramString.length();
		this.buffer = new byte[j + 3];

		this.buffer[(i++)] = STX;
		this.bcc = 2;

		for (int m = 0; m < j; m++) {
			int k = (byte) paramString.charAt(m);
			this.buffer[(i++)] = (byte) k;
			this.bcc = ((byte) (this.bcc ^ k));
		}

		this.buffer[(i++)] = ETX;
		this.bcc = ((byte) (this.bcc ^ 0x3));
		this.buffer[(i++)] = this.bcc;
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public void iniciarRecepcao() {
		this.passo = 0;
		this.contador = 0;
		this.ok = false;
	}

	public boolean acrescentarByte(byte paramByte) {
		boolean bool = false;
		
		if (this.contador == this.maximo) {
			return true;
		}

		switch (this.passo) {
		case 0:
			if (paramByte == 2) {
				this.buffer[(this.contador++)] = paramByte;
				this.bcc = paramByte;
				this.passo += 1;
			}
			break;
		case 1:
			this.buffer[(this.contador++)] = paramByte;
			this.bcc = ((byte) (this.bcc ^ paramByte));
			if (paramByte == 3) {
				this.passo += 1;
			} else if (paramByte == 2) {
				this.contador = 0;
				this.buffer[(this.contador++)] = paramByte;
			}
			break;
		case 2:
			this.buffer[(this.contador++)] = paramByte;
			if (paramByte == this.bcc) {
				this.ok = true;
			}
			bool = true;
		}
		
		return bool;
	}

	public boolean getRecepcaoOk() {
		return this.ok;
	}

	public String getResposta() {
		return new String(this.buffer, 1, this.contador - 3);
	}
	
}