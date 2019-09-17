package br.com.pertocheck.lyceum;

import java.io.Serializable;

public class PChek implements Serializable {

	private static final long serialVersionUID = -7621055539938945358L;

	private String porta;
	private int taxaTransmissao;

	private PchekComm pchek;
	private boolean comunicacaoOk = false;
	private boolean debug;

	public boolean init(String pPorta, int pTaxaTransmissao, boolean debug) {
		this.debug = debug;
		this.porta = pPorta;
		this.taxaTransmissao = pTaxaTransmissao;

		if (this.porta == null) {
			String sitemaOperacional = System.getProperty("os.name");

			this.debug("Sistema Operacional = " + sitemaOperacional);

			if ("Linux".equalsIgnoreCase(sitemaOperacional)) {
				this.porta = "/dev/ttyS0";
			} else {
				this.porta = "COM4";
			}
		}

		this.pchek = new PchekComm(this.porta, this.taxaTransmissao, 500);
		
		boolean isComunicacao = false;
		if (this.pchek.isComunicacao()) {
			isComunicacao = true;
		}
		
		return isComunicacao;
	}

	public void stop() {
		this.debug("Terminando o Pchek!");
		this.pchek.terminate();
	}

	public boolean enviarComando(String comando, int timeout) {
		this.debug("Comando: " + comando + " Timeout: " + timeout);

		if (this.pchek.processarComando(comando, timeout)) {
			if (debug) {
				this.debug("C�digo Resposta: " + this.pchek.getResposta());
				System.out.println("C�digo Resposta: \"" + this.pchek.getResposta() + "\"");
			}

			if (this.pchek.getResposta().substring(1, this.pchek.getResposta().length()).length() <= 3) {
				this.debug("Resposta: " + switchMensagem(
						Integer.parseInt(this.pchek.getResposta().substring(1, this.pchek.getResposta().length()))));
				System.out.println("Resposta: \""
						+ switchMensagem(Integer
								.parseInt(this.pchek.getResposta().substring(1, this.pchek.getResposta().length())))
						+ "\"");
			} else {
				this.debug("Resposta: " + this.pchek.getResposta());
				System.out.println("Resposta: \"" + this.pchek.getResposta() + "\"");
			}

			this.comunicacaoOk = true;
		} else {
			this.debug("[" + this.pchek.getErro() + "] " + this.pchek.getErro());
			System.out.println("[" + this.pchek.getErro() + "] " + this.pchek.getErro());
			this.comunicacaoOk = false;
		}
		return this.comunicacaoOk;
	}

	public String getResposta() {
		if (this.comunicacaoOk) {
			return this.pchek.getResposta();
		}
		
		return null;
	}

	private void debug(String paramString) {
		if (this.debug) {
			System.out.println(paramString);
		}
	}

	public boolean hasMensagemRetornoLeitura() {
		if (this.pchek.getResposta() != null && this.pchek.getResposta().contains("=")) {
			if (this.pchek.getResposta().replace("=", "").length() == 3) {
				return true;
			}
		}
		return false;
	}

	public boolean hasMensagemRetornoLeituraCMC7() {
		if (this.pchek.getResposta() != null && this.pchek.getResposta().startsWith("P")) {
			if (this.pchek.getResposta().replace("P", "").length() == 3) {
				return true;
			}
		}
		return false;
	}

	public String switchMensagem(Integer retorno) {
		switch (retorno) {
		case 0: {
			return "Sucesso na execu��o do comando.";
		}
		case 1: {
			return "Mensagem com dados inv�lidos.";
		}
		case 2: {
			return "Tamanho de mensagem inv�lido.";
		}
		case 5: {
			return "Leitura dos caracteres magn�ticos inv�lida.";
		}
		case 6: {
			return "Problemas no acionamento do motor 1.";
		}
		case 8: {
			return "Problemas no acionamento do motor 2.";
		}
		case 9: {
			return "Banco diferente do solicitado.";
		}
		case 11: {
			return "Sensor 1 obstru�do.";
		}
		case 12: {
			return "Sensor 2 obstru�do.";
		}
		case 13: {
			return "Sucesso na execu��o do comando.";
		}
		case 14: {
			return "Erro no posicionamento da cabe�a de impress�o (relativo a S4).";
		}
		case 16: {
			return "D�gito verificador do cheque n�o confere.";
		}
		case 17: {
			return "Aus�ncia de caracteres magn�ticos ou cheque na posi��o errada.";
		}
		case 18: {
			return "Tempo esgotado.";
		}
		case 19: {
			return "Documento mal inserido.";
		}
		case 20: {
			return "Cheque preso durante o alinhamento (S1 e S2 desobstru�dos).";
		}
		case 21: {
			return "Cheque preso durante o alinhamento (S1 obstru�do e S2 desobstru�do).";
		}
		case 22: {
			return "Cheque preso durante o alinhamento (S1 desobstru�do e S2 obstru�do).";
		}
		case 23: {
			return "Cheque preso durante o alinhamento (S1 e S2 obstru�dos).";
		}
		case 24: {
			return "Cheque preso durante o preenchimento (S1 e S2 desobstru�dos).";
		}
		case 25: {
			return "Cheque preso durante o preenchimento (S1 obstru�do e S2 desobstru�do).";
		}
		case 26: {
			return "Cheque preso durante o preenchimento (S1 desobstru�do e S2 obstru�do).";
		}
		case 27: {
			return "Cheque preso durante o preenchimento (S1 e S2 obstru�dos).";
		}
		case 28: {
			return "Caractere inexistente.";
		}
		case 30: {
			return "N�o h� cheques na mem�ria.";
		}
		case 31: {
			return "Lista negra interna cheia";
		}
		case 42: {
			return "Cheque ausente.";
		}
		case 43: {
			return "PINPad ou teclado ausente.";
		}
		case 50: {
			return "Erro de transmiss�o.";
		}
		case 51: {
			return "Erro de transmiss�o: Impressora offline, desconectada ou ocupada.";
		}
		case 52: {
			return "Erro no pin pad.";
		}
		case 60: {
			return "Cheque na lista negra.";
		}
		case 73: {
			return "Cheque n�o encontrado na lista negra.";
		}
		case 74: {
			return "Comando cancelado.";
		}
		case 84: {
			return "Arquivo de layout�s cheio.";
		}
		case 85: {
			return "Layout inexistente na mem�ria.";
		}
		case 91: {
			return "Leitura de cart�o inv�lida.";
		}
		case 92: {
			return "Erro na leitura da trilha 1 (somente para leitora 2 trilhas).";
		}
		case 93: {
			return "Erro na leitura da trilha 2 (somente para leitora 2 trilhas).";
		}
		case 94: {
			return "Erro na leitura da trilha 3 (somente para leitora 2 trilhas).";
		}
		case 97: {
			return "Cheque na posi��o errada.";
		}
		case 111: {
			return "PINPad n�o retornou EOT.";
		}
		case 150: {
			return "PINPad n�o retornou ACK.";
		}
		case 155: {
			return "PINPad n�o responder.";
		}
		case 171: {
			return "Tempo esgotado na resposta do PINPad.";
		}
		case 253: {
			return "Erro em equipamento fiscal (Sem cidade, Falta redu��o Z, etc....).";
		}
		case 255: {
			return "Comando inexistente.";
		}
		default: {
			return "Erro desconhecido!";
		}
		}
	}

	public String retornaMensagem() {
		if (hasMensagemRetornoLeitura()) {
			String mensagem = switchMensagem(Integer.parseInt(this.pchek.getResposta().replace("=", "")));
			this.debug("Mensagem retorno leitura cheque: " + mensagem);
			return mensagem;
		}
		return "N�o h� mensagens de retorno!";
	}

	public String retornaMensagemCMC7() {
		if (hasMensagemRetornoLeitura()) {
			String mensagem = switchMensagem(Integer.parseInt(this.pchek.getResposta().replace("P", "")));
			this.debug("Mensagem retorno leitura CMC7: " + mensagem);
			return mensagem;
		}
		return "N�o h� mensagens de retorno!";
	}

}