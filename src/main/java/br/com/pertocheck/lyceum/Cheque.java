package br.com.pertocheck.lyceum;

import java.io.Serializable;

public class Cheque implements Serializable {

	private static final long serialVersionUID = -1593527402790294589L;

	private String banco;
	private String agencia;
	private String conta;
	private String chequeNumero;
	private String compensacao;

	public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getConta() {
		return conta;
	}

	public void setConta(String conta) {
		this.conta = conta;
	}

	public String getChequeNumero() {
		return chequeNumero;
	}

	public void setChequeNumero(String chequeNumero) {
		this.chequeNumero = chequeNumero;
	}

	public String getCompensacao() {
		return compensacao;
	}

	public void setCompensacao(String compensacao) {
		this.compensacao = compensacao;
	}

	@Override
	public String toString() {
		return "Cheque [banco=" + banco + ", agencia=" + agencia + ", conta=" + conta + ", chequeNumero=" + chequeNumero
				+ ", compensacao=" + compensacao + "]";
	}

}