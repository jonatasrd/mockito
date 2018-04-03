package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();

		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);

		when(dao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}

	@Test
	public void naoDeveEcerrarLeiloesQueComecaramMenosDeUmasemana() {
		Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();

		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);

		when(dao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();

		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilao1.isEncerrado());
		assertFalse(leilao2.isEncerrado());

		verify(dao, never()).atualiza(leilao1);
		verify(dao, never()).atualiza(leilao2);
	}

	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);

		when(dao.correntes()).thenReturn(new ArrayList<Leilao>());
		EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(dao, carteiro);
		encerradorDeLeilao.encerra();
		assertEquals(0, encerradorDeLeilao.getTotalEncerrados());
	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();

		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiro = mock(EnviadorDeEmail.class);

		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiro);
		encerrador.encerra();

		// verificando que o metodo atualiza foi realmente invocado, somente uma
		// vez!
		verify(daoFalso, times(1)).atualiza(leilao1);
	}
}
