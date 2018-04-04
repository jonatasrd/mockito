package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {

	private Calendar antiga;
	private Leilao leilaoAntigo1;
	private Leilao leilaoAntigo2;
	private RepositorioDeLeiloes dao;
	private Carteiro carteiro;
	private EncerradorDeLeilao encerrador;
	private Leilao leilaoOntem1;
	private Leilao leilaoOntem2;
	private Calendar ontem;

	@Before
	public void setup() {
		antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);

		leilaoAntigo1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		leilaoAntigo2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		leilaoOntem1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		leilaoOntem2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

		dao = mock(RepositorioDeLeiloes.class);
		carteiro = mock(Carteiro.class);
		encerrador = new EncerradorDeLeilao(dao, carteiro);
	}

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {

		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1, leilaoAntigo2));
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilaoAntigo1.isEncerrado());
		assertTrue(leilaoAntigo2.isEncerrado());
	}

	@Test
	public void naoDeveEcerrarLeiloesQueComecaramMenosDeUmasemana() {

		when(dao.correntes()).thenReturn(Arrays.asList(leilaoOntem1, leilaoOntem2));
		encerrador.encerra();
		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilaoOntem1.isEncerrado());
		assertFalse(leilaoOntem2.isEncerrado());
		verify(dao, never()).atualiza(leilaoOntem1);
		verify(dao, never()).atualiza(leilaoOntem2);
	}

	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		when(dao.correntes()).thenReturn(new ArrayList<Leilao>());
		encerrador.encerra();
		assertEquals(0, encerrador.getTotalEncerrados());
	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {
		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1));
		encerrador.encerra();
		verify(dao, times(1)).atualiza(leilaoAntigo1);
	}

	@Test
	public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1));
		encerrador.encerra();
		InOrder inOrder = inOrder(dao, carteiro);
		inOrder.verify(dao, times(1)).atualiza(leilaoAntigo1);
		inOrder.verify(carteiro, times(1)).envia(leilaoAntigo1);
	}

	@Test
	public void deveContinuarAExecucaoAposFalhaDoDao() {
		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1, leilaoAntigo2));
		doThrow(new RuntimeException()).when(dao).atualiza(leilaoAntigo1);
		encerrador.encerra();
		verify(dao).atualiza(leilaoAntigo2);
		verify(carteiro).envia(leilaoAntigo2);
	}

	@Test
	public void deveContinuarAExecucaoAposFalhaCorreio() {
		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1, leilaoAntigo2));
		doThrow(new RuntimeException()).when(carteiro).envia(leilaoAntigo1);
		encerrador.encerra();
		verify(dao).atualiza(leilaoAntigo2);
		verify(carteiro).envia(leilaoAntigo2);
	}

	@Test
	public void deveGarantirQueOCorreioNuncaEInvocado() {
		when(dao.correntes()).thenReturn(Arrays.asList(leilaoAntigo1, leilaoAntigo2));
		doThrow(new RuntimeException()).when(dao).atualiza(any(Leilao.class));
		encerrador.encerra();
		verify(carteiro, never()).envia(any(Leilao.class));
	}
}
