package teste01;


import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
import java.text.SimpleDateFormat;


public class reservas {
 


	//************************ Variaveis ***********************
	//arrays de salas.txt
	private static int[]     salaIdSala = new int[100];                 //coluna1
	private static String[]  salaNomeSala = new String[100];            //coluna2
	private static int[]     salaFilas = new int[100];                  //coluna3
	private static int[]     salaLugares = new int[100];                //coluna4
	private static String[]  salaNomeEspetaculo = new String[100];      //coluna5
	private static int[]     salaPrecoBilhete = new int[100];           //coluna6
	//novos arrays
	private static int[]     salaTotalLugares = new int[100];           //coluna1

	//arrays de reservas.txt
	private static int[]     reservaIdSala = new int[100];              //coluna1
	private static String[]  reservaNomeCliente = new String[100];      //coluna2
	private static char[]    reservaCondicao = new char[100];           //coluna3
	private static int[]     reservaFila = new int[100];                //coluna4
	private static int[]     reservaLugar = new int[100];               //coluna5
	private static int[]     reservaDia = new int[100];                 //coluna6
	private static int[]     reservaMes = new int[100];                 //coluna7
	private static char[]    reservaSessao = new char[100];             //coluna8

	//array clientes
	private static String[]	clientesNome = new String[100];

	//declara variaveis
	private static int num_salas = 0;
	private static int num_reservas = 0;
	private static int num_clientes = 0;

	//********************** Funcoes ***************************
	//Converte um char array em int, vamos usar nas reservas multiplas, em digito
        //source: http://stackoverflow.com/questions/2683324/java-char-array-to-int
	private static int charParaInt(char numeroChar) {
		return (int)  Integer.parseInt(String.valueOf(numeroChar));
                //outro metodo, usando substituicao de unicode: return (int) numeroChar-'0';
        }

	//Dado um codigo de sessao em char devolve o equivalente numerico
        //nota: http://stackoverflow.com/questions/18192255/regarding-java-switch-statements-using-return-and-omitting-breaks-in-each-case
	private static int sessaoNumero(char sessaoChar) {
		switch (sessaoChar) {
		case 'M': return 0;
		case 'T': return 1;
		case 'N': return 2;
		}
		return -1; //Erro. tipo de sessÃ£o nao esperada.
	}

	//Dado um codigo de sessao em int devolve o equivalente em char
	private static char sessaoChar(int sessaoInt) {
		switch (sessaoInt) {
		case 0: return 'M';
		case 1: return 'T';
		case 2: return 'N';
		}
		return '\0'; //Char 0 se Erro
	}
 
	//Dado um nome de cliente devolve o seu id
	//o id E o indice no array de clientes + 1
	//Nao podemos devolver o indice porque pode ser 0 . 0 vai ser utilizado para representar lugar livre
        //source: http://stackoverflow.com/questions/513832/how-do-i-compare-strings-in-java
	private static int procuraCliente(String nomeCliente) {
		for (int c=0; c<num_clientes; c++) {
			if (clientesNome[c].equals(nomeCliente))  // em string, uma comparacao e com equals, a semelhanca de == em int
				return c+1;
		}
		return -1; //Erro. Cliente nao encontrado
	}

	//Devolve uma String com a representacao do mapa de uma sala
	private static String imprimirMapa(int[][] mapa, int f_max, int l_max) {
		String mapaString="";
		int comprimento;
		for (int f=0; f<f_max; f++) {
			for (int l=0; l<l_max; l++) {
				if (mapa[f][l]==0)
					mapaString+="L | ";
				else if (mapa[f][l]>0)
					mapaString+=("C | ");
				else
					mapaString+=("R | ");
			}
			//removemos | ao final da linha. (removeLastChar do stackoverflow)
			comprimento=mapaString.length();
			mapaString=mapaString.substring(0,comprimento-2);
			//nova linha para nova fila
			mapaString+="\n";
		}
		return mapaString;
	}
	
	//Funcao que conta o numero de lugares livres, reservas, e compras
	private static int[] contarEstados(int[][] mapa, int max_filas, int max_lugares) {
		int[] count = {0,0,0};   // (livres, reservados, comprados)
		//indice 0 para livre (valor 0 no mapa)
		//indice 1 para reservado (valor negativo no mapa)
		//indice 2 para comprado (valor positivo no mapa)
		for (int f=0; f<max_filas; f++)
			for (int l=0; l<max_lugares; l++)
				if (mapa[f][l]==0)              // lugares livres. igual a zero.
					count[0]=count[0]+1;
				else if (mapa[f][l]<0)          // lugares reservados. menor que zero.
					count[1]=count[1]+1;
				else                            // lugares comprados . maior que zero.
					count[2]=count[2]+1;
		return count;
	}
	
	//Funcao que dado um mapa e um cliente determina as reservas do cliente
	private static String procuraReservas(int[][] mapa, int max_filas, int max_lugares, int cliente) {
		String reservas="";
		for (int f=0; f<max_filas; f++)
			for (int l=0; l<max_lugares; l++)
				if (mapa[f][l]==-cliente)
					reservas += " | " + (f+1) + " | " + (l+1); 
		return reservas;
	}
	
	//Funcao que recebe os calculos e os escreve num ficheiro
        //nota: ficheiro criado em UTF8, de acordo com instruções de FP pratica, aula13. temos de abrir com notepad++, que reconhece utf-8.
        //nota: documentação em utf-8: https://en.wikipedia.org/wiki/UTF-8 . character encoding em unicode.
	private static void escreveOutput(String outputParaFicheiro) {
                //source: http://stackoverflow.com/questions/2885173/how-to-create-a-file-and-write-to-a-file-in-java
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter("resultados" + now() + ".txt", "UTF-8");
                } catch (Exception e) {
                    System.out.println(e);
                }
                writer.println(outputParaFicheiro);
                writer.close();
	}
        
        //Função que prepara a data e hora atual
        //source: enunciado do trabalho. formato da data modificado para corresponder ao pedido.
        public static final String DATE_FORMAT = "yyyyMMddHHmmss";
                public static String now() {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                return sdf.format(cal.getTime());
        }
    
	public static void main(String[] args) {
		int i, j;
		//******************** salas.txt ****************************************
		// salas.txt : le dados do ficheiro
                // layout do try catch: https://docs.oracle.com/javase/tutorial/essential/exceptions/catch.html
		Scanner scanner;
		try	{
		    scanner = new Scanner( new File("salas.txt") ).useDelimiter( "\\s*:\\s*|\\s*\n\\s*" );
		} catch( FileNotFoundException ex ) {
		    System.out.println( ex );
		    return;
		}
		scanner.skip( "\\s*" );
		
		//salas.txt: separa o conteudo da variavel scanner em arrays diferentes.
                //nota: i=1 porque nao existe sala 0! 
		for( i = 1;  scanner.hasNextLine();  i++ ){
		    salaIdSala[i]         = scanner.nextInt();      //coluna1
		    salaNomeSala[i]       = scanner.next();         //coluna2
		    salaFilas[i]          = scanner.nextInt();      //coluna3
		    salaLugares[i]        = scanner.nextInt();      //coluna4
		    salaNomeEspetaculo[i] = scanner.next();         //coluna5
		    salaPrecoBilhete[i]   = scanner.nextInt();      //coluna6

		    scanner.skip( "\\s*" );
		    num_salas++;
		}

		// salas.txt : output para ecra.
		//System.out.println("*** salas ***");
		//System.out.println("\tC1:idSala \tC2:nomeSala \tC3:fila \tC4:lugares \tC5:nomeEspetaculo \tC6:precoBilhete");
		//for( i = 0;  i < num_salas;  i++ ){
		//    System.out.print  ( "L" + i + ":\t") ;
		//    System.out.print  ( "C1:" + salaIdSala[i]  +          "\t\t");
		//    System.out.print  ( "C2:" + salaNomeSala[i] +         "\t");
		//    System.out.print  ( "C3:" + salaFilas[i] +            "\t\t");
		//    System.out.print  ( "C4:" + salaLugares[i] +          "\t\t");
		//    System.out.print  ( "C5:" + salaNomeEspetaculo[i] +   "\t\t");
		//    System.out.println( "C6:" + salaPrecoBilhete[i]  +    "\t");
		//}

		//********************* reservas.txt **************************************                
		// reservas.txt : le dados do ficheiro
                // layout do try catch: https://docs.oracle.com/javase/tutorial/essential/exceptions/catch.html
		Scanner scannerReservas;
		try {
		    scannerReservas = new Scanner( new File("reservas.txt") ).useDelimiter( "\\s*:\\s*|\\s*\n\\s*" );
		} catch( FileNotFoundException ex ) {
		    System.out.println( ex );
		    return;
		}
		scannerReservas.skip( "\\s*" );

		// reservas.txt: separa o conteudo da variavel scanner em arrays diferentes.
		for( j = 0;  scannerReservas.hasNextLine();  j++ ){
		    reservaIdSala[j] = scannerReservas.nextInt();           //coluna1
		    reservaNomeCliente[j] = scannerReservas.next();         //coluna2
		    reservaCondicao[j] = scannerReservas.next().charAt(0);  //coluna3
		    reservaFila[j]  = scannerReservas.nextInt();            //coluna4
		    reservaLugar[j]  = scannerReservas.nextInt();           //coluna5
		    reservaDia[j]    = scannerReservas.nextInt();           //coluna6
		    reservaMes[j]   = scannerReservas.nextInt();            //coluna7
		    reservaSessao[j] = scannerReservas.next().charAt(0);    //coluna8

		    scannerReservas.skip( "\\s*" );
		    num_reservas++;
		}

		// reservas.txt : output para ecra
//                System.out.println("*** reservas.txt ***");
//		for( i = 0;  i < num_reservas ;  i++ ){
//		    System.out.print  ( "L" + i + ":\t") ;
//		    System.out.print  ( "C1:" + reservaIdSala[i]            + "\t");
//		    System.out.print  ( "C2:" + reservaNomeCliente[i]       + "\t");
//		    System.out.print  ( "C3:" + reservaCondicao[i]          + "\t");
//		    System.out.print  ( "C4:" + reservaFila[i]              + "\t");
//		    System.out.print  ( "C5:" + reservaLugar[i]             + "\t");
//		    System.out.print  ( "C6:" + reservaDia[i]               + "\t");
//		    System.out.print  ( "C7:" + reservaMes[i]               + "\t");
//		    System.out.println( "C8:" + reservaSessao[i]            + "\t");
//		}
		//*********************** Output ****************************
                
		//Criacaoo de lista de clientes
		for (i=0; i<num_reservas; i++) {
			if (procuraCliente(reservaNomeCliente[i])==-1) {
				clientesNome[num_clientes]=reservaNomeCliente[i];
				num_clientes++;
			}
		}

                //***** mostra lista de clientes no ecra *****
//		System.out.println("*** lista de clientes ***");
//                for (i=0; i<num_clientes; i++)
//			System.out.println(clientesNome[i]);

                String output_total= "";
		
		int[][] mapa = new int[100][100];
		int fila, max_filas;
		int lugar, max_lugares;
		
                //totais globais
                int totalReceitaVendidos = 0; 
                int totalReceitaEstimado = 0;
                
		int idCliente;
		
		boolean reservaEncontrada=false;
		int reservasProcessadas=0;
		
		for (int sala=1; sala<=num_salas; sala++) {
			max_filas=salaFilas[sala];   //maximofilas vem da parte do array das salasfilas
			max_lugares=salaLugares[sala];   //maximolugares vem da parte do array das salaslugares
			for (int mes=1; mes<=12; mes++) { // ve os meses todos ate ser o mes 12
				for (int dia=1; dia<=31; dia++) {  // vai ver os dias todos ate ser dia 31 (nao limita meses com < 30 dias)
					for (int sessao=0; sessao<3; sessao++) {  // para os tres tipos de sessao (manha.tarde.noite)
						//inicializar mapa a zeros (lugares vazios)
						for (i=0; i<100; i++)
							for (j=0; j<100; j++)
								mapa[i][j]=0; //matriz de maximo 100 por 100
						
						for (int r=0; r<num_reservas; r++) {    // num_reservas e o numero de linhas no file reservas.txt
							if ( 	reservaIdSala[r]==sala && // reservaidsala e o numero que vai corresponder a cor da sala
									reservaMes[r]==mes && //reservames vai ser comparado ao mes que estamos
									reservaDia[r]==dia && // reservadia vai ser comparada ao dia em que se mete
									sessaoNumero(reservaSessao[r])==sessao ) { //sessaonumero vai ser numeros q comparam a sessao(0=M,1=T,2=N)
								
								reservasProcessadas++;
								reservaEncontrada=true;// se encontra reserva  
								
								idCliente=procuraCliente(reservaNomeCliente[r]);
								fila=reservaFila[r]-1; // fila-1 pois n ha fila0 ( posicao0=posicaofila1 )
								lugar=reservaLugar[r]-1;// lugar-1 pois n ha lugar0 (posicao0=posicaolugar1 )
								
								switch (reservaCondicao[r]) {
								case 'A':
									mapa[fila][lugar]=0;
									break;
								case 'C':
									mapa[fila][lugar]=idCliente;
									break;
								case 'R':
									mapa[fila][lugar]=-idCliente;
									break;
								default:
									int numLugares=charParaInt(reservaCondicao[r]);
									for (int l=0; l<numLugares; l++)
										mapa[fila][lugar+l]=-idCliente;
								}
							}
						}
						
						if (reservaEncontrada) {
							//criamos e inizializamos o array que vai conter a contagem
							//de livres(0) reservas(1) comprados(2)
							int[] countEstado;
							String output="";
							
							//<Nome Espetactaculo> | <Nome Sala> | <Numero Filas> | <Numero Lugares>
							output += salaNomeEspetaculo[sala] + " | ";
							output += salaNomeSala[sala] + " | ";
							output += salaFilas[sala] + " | ";
							output += salaLugares[sala] + "\n";
							
							//<Dia> | <Mes> | <Sessao>
							output += dia + " | " + mes + " | " + sessaoChar(sessao) + "\n";
							
							//Mapa da sala
							output += imprimirMapa(mapa, max_filas, max_lugares);
							
							//Determinamos a contagem dos estados (livre, reservado, comprado)
							countEstado = contarEstados(mapa, max_filas, max_lugares);
							
							//Contagem de lugares livres
                                                        //livres(0) reservas(1) comprados(2)
							output += countEstado[0] + "\n";
							
							//Detalhes dos lugares reservados
							String clienteReservas;
							for (int c=1; c<=num_clientes; c++) {
								clienteReservas=procuraReservas(mapa, max_filas, max_lugares, c);
								
								if (!clienteReservas.isEmpty()) {
                                                                        //O C=1 comeca com a posicao de array 0.
                                                                        //O cliente tem de ser como 1 pois nao ha cliente 0.
                                                                        //Para comecarmos com o posicao 0 temos de fazer c-1 pois (1-1)=0
									output += clientesNome[c-1] + " | Reserva" + clienteReservas + "\n";
								}
							}
							
							//<Receita> | <Estimativa>
							int receitaVendidos = countEstado[2]*salaPrecoBilhete[sala];
							int receitaReservados = countEstado[1]*salaPrecoBilhete[sala] ;

                                                        //*** inicio do codigo de total de receitas *** 
                                                        totalReceitaVendidos += receitaVendidos; 
                                                        totalReceitaEstimado += (receitaVendidos + receitaReservados);
                                                        
							output += receitaVendidos + " | " + (receitaVendidos+receitaReservados);
							
							//<Linha em branco>
							output += "\n\n";
							output_total+=output;
						}
						
                         
						reservaEncontrada=false;
						if (reservasProcessadas==num_reservas) {
							//<Receita Total> | <Estimativa Total>
                                                        //codigo de todas as sessoes:calcula o total de receitascompradas | receitas compradas + reservadas 
                                                        output_total+=(totalReceitaVendidos +" | " +  totalReceitaEstimado);
                                                        
							escreveOutput(output_total);
                                                        System.out.println(output_total);
                                                        
                                                        //nota: http://stackoverflow.com/questions/3715967/when-should-we-call-system-exit-in-java
                                                        System.exit(0);
						}
					}
				}
			}
		}
	}
}