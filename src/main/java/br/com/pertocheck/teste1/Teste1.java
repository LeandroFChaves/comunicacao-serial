package br.com.pertocheck.teste1;

public class Teste1 {

	public static void main(String[] args) {
		escrita();
		//leitura();
	}

	public static void leitura() {
		// Iniciando leitura serial
		SerialComLeitura leitura = new SerialComLeitura("COM4", 4800, 60);

		leitura.HabilitarLeitura();
		leitura.ObterIdDaPorta();
		leitura.AbrirPorta();
		leitura.LerDados();

		// Controle de tempo da leitura aberta na serial
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			System.out.println("Erro na Thread: " + ex);
		}

		leitura.FecharCom();
	}

	public static void escrita() {
		SerialComLeitura serialEscrita = new SerialComLeitura("COM4", 4800, 60);

		serialEscrita.HabilitarEscrita();
		serialEscrita.ObterIdDaPorta();
		serialEscrita.AbrirPorta();
		serialEscrita.EnviarUmaString(">");
		serialEscrita.FecharCom();
	}
}
