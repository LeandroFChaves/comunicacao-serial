package br.com.pertocheck.lyceum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Pertocheck {

	public static PChek pertoCheck;

	public static void main(String args[]) {
		pertoCheck = new PChek();
		boolean isOk = pertoCheck.init("COM3", 4800, true); 
		
		if (isOk) {
			lerCheque();
			//lerCMC7();
			//imprimirCheque("1234", "210919", "Sao Paulo", "Techne - Lyceum");
			pertoCheck.stop();
		}
	}

	public static boolean lerCheque() {
		// Envia o comando de leitura do cheque
		pertoCheck.enviarComando("=", 50);

		// Valida se ha mensagens de alerta na leitura e pega o retorno
		if (!pertoCheck.hasMensagemRetornoLeitura()) {
			String retornoLeitura = pertoCheck.getResposta().replace("=", "");

			String banco = retornoLeitura.substring(0, 6);
			String agencia = retornoLeitura.substring(6, 10);
			String conta = retornoLeitura.substring(10, 20);
			String chequeNumero = retornoLeitura.substring(20, 26);
			String compensacao = retornoLeitura.substring(26, 29);

			Cheque cheque = new Cheque();
			cheque.setBanco(banco);
			cheque.setAgencia(agencia);
			cheque.setConta(conta);
			cheque.setChequeNumero(chequeNumero);
			cheque.setCompensacao(compensacao);

			System.out.println("Cheque: " + cheque);
		} else {
			System.out.println("Mensagem: " + pertoCheck.retornaMensagem());
		}

		// Envia o comando que retira o cheque da máquina
		return pertoCheck.enviarComando(">", 50);
	}

	public static boolean imprimirCheque(String valorCheque, String dataPrevisaoCheque, String cidade,
			String beneficiario) {
		System.out.println("Valor cheque: " + valorCheque);
		System.out.println("Data previsao cheque: " + dataPrevisaoCheque);

		if (isNotNullOrEmpty(valorCheque) && isNotNullOrEmpty(dataPrevisaoCheque)) {
			System.out.println("Início impressão do cheque!");

			// Envia o comando para preencher a data
			if (!pertoCheck.enviarComando("!" + dataPrevisaoCheque, 50)) {
				return false;
			}

			// Valida se nao houve erro
			if (!pertoCheck.getResposta().equals("!000")) {
				System.out.println("Erro na resposta! " + pertoCheck.getResposta());
				return false;
			}

			// Caso o benefeciario for preenchido, imprimir no cheque
			if (isNotNullOrEmpty(beneficiario)) {
				// Envia comando para preencher o benefeciario
				if (!pertoCheck.enviarComando("%" + beneficiario, 50)) {
					return false;
				}

				// Valida se houve erro
				if (!pertoCheck.getResposta().equals("%000")) {
					System.out.println("Erro na resposta! " + pertoCheck.getResposta());

					return false;
				}
			}

			// Caso a cidade for preenchida, imprimir no que
			if (isNotNullOrEmpty(cidade)) {
				// Envia comando para preencher a cidade
				if (!pertoCheck.enviarComando("#" + cidade, 50)) {
					return false;
				}

				// Valida se houve erro
				if (!pertoCheck.getResposta().equals("#000")) {
					System.out.println("Erro na resposta! " + pertoCheck.getResposta());

					return false;
				}
			}

			// Envia o comando para ler o cheque
			if (!pertoCheck.enviarComando("=", 50)) {
				return false;
			}

			// Valida se nao houve erro
			if (!pertoCheck.getResposta().substring(0, 4).equals("=000")) {
				System.out.println("Erro na resposta! " + pertoCheck.getResposta());

				return false;
			}

			// Envia o comando para imprimir o cheque
			if (!pertoCheck.enviarComando(";2" + doubleToStringFormated(Double.parseDouble(valorCheque)), 50)) {
				return false;
			}

			// Valida se houve erro
			if (!pertoCheck.getResposta().substring(0, 4).equals(";000")) {
				System.out.println("Erro na resposta! " + pertoCheck.getResposta());

				return false;
			}

			// Caso executado tudo com sucesso, retorna mensagem de sucesso
			System.out.println("Cheque preenchido com sucesso!");

			return true;

		}
		System.out.println("Valor do cheque ou data não preenchido!");

		return false;
	}

	public static boolean lerCMC7() {
		// Envia o comando de leitura do cheque
		pertoCheck.enviarComando("P", 50);

		// Valida se ha mensagens de alerta na leitura e pega o retorno
		System.out.println("Mensagem retorno leitura CMC7: " + !pertoCheck.hasMensagemRetornoLeituraCMC7());

		if (!pertoCheck.hasMensagemRetornoLeituraCMC7()) {
			String retornoLeitura = removeLettersFromString(pertoCheck.getResposta());

			retornoLeitura = retornoLeitura.substring(3, retornoLeitura.length());
			
			System.out.println("CMC7: " + retornoLeitura);
		} else {
			System.out.println("Mensagem: " + pertoCheck.retornaMensagemCMC7());
		}

		// Envia o comando que retira o cheque da maquina
		return pertoCheck.enviarComando(">", 50);
	}

	public static boolean isNotNullOrEmpty(String string) {
		if (string != null && !string.isEmpty()) {
			return true;
		}

		return false;
	}

	public static String doubleToStringFormated(double value) {
		try {
			NumberFormat nf = new DecimalFormat("##########,##0000000000.00",
					new DecimalFormatSymbols(new Locale("en", "EU")));
			return nf.format(value).replace(".", "");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Metodo que remove letras de uma String
	 * 
	 * @param string
	 * @return Long
	 */
	public static String removeLettersFromString(String string) {
		if (string != null && !string.isEmpty()) {
			string = string.replaceAll("[^0-9]", "");

			return string;
		}

		return null;
	}
}