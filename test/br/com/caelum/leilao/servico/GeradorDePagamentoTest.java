package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.relogio.Relogio;

public class GeradorDePagamentoTest {
	
	private RepositorioDeLeiloes leiloes;
	private RepositorioDePagamentos pagamentos;
	private Avaliador avaliador;
	private Leilao leilao;
	private Relogio relogio;


	@Before
	public void setup(){
		
		leiloes = mock(RepositorioDeLeiloes.class);
		pagamentos = mock(RepositorioDePagamentos.class);
		avaliador = mock(Avaliador.class);
		relogio = mock(Relogio.class);
		
		leilao = new CriadorDeLeilao().para("Playstation").lance(new Usuario("Jos� da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0).constroi();
		
	}

	@Test
	public void deveGerarPagamentoParaUmLeilaoEncerrado() {

		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));
		when(avaliador.getMaiorLance()).thenReturn(2500.0);

		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador());
		gerador.gera();

		// como fazer assert no Pagamento gerado?
		// criamos o ArgumentCaptor que sabe capturar um Pagamento
	    ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
	    
	    // capturamos o Pagamento que foi passado para o m�todo salvar
	    verify(pagamentos).salva(argumento.capture());
	    
        Pagamento pagamentoGerado = argumento.getValue();
        assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}
	
	
	@Test
	public void deveEmpurrarParaOProximoDiaUtilQuandoPagamentoCairNoSabado(){
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

		Calendar sabado = Calendar.getInstance();
		sabado.set(2018, Calendar.APRIL, 28);
		
		when(relogio.hoje()).thenReturn(sabado);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(30, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}
	
	@Test
	public void deveEmpurrarParaOProximoDiaUtilQuandoPagamentoCairNoDomingo(){
		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		
		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

		Calendar domingo = Calendar.getInstance();
		domingo.set(2018, Calendar.APRIL, 29);
		
		when(relogio.hoje()).thenReturn(domingo);
		
		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogio);
		gerador.gera();
		
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salva(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();
		
		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
		assertEquals(30, pagamentoGerado.getData().get(Calendar.DAY_OF_MONTH));
	}

}
