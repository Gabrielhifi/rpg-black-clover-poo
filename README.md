🍀 RPG Black Clover - Terminal Edition

Um RPG interativo via terminal baseado no universo de Black Clover, desenvolvido inteiramente em Java. Este projeto tem como foco principal a aplicação prática de Programação Orientada a Objetos (POO) e dos princípios SOLID, criando uma arquitetura de código limpa, escalável e de fácil manutenção.

🎮 Sobre o Jogo

Neste mini-RPG, você assume o papel de um Cavaleiro Mágico do Reino Clover e deve enfrentar inimigos poderosos (como membros do Olho do Sol da Meia-Noite) em um combate por turnos.

Personagens Jogáveis:

Asta (Cavaleiro Anti-Magia): Focado em dano físico bruto e ataques especiais massivos sem consumo de mana.

Yuno (Cavaleiro Mágico): Utiliza magias de vento poderosas, gerenciando uma reserva de Mana para lançar seus ataques.



🏗️ Arquitetura e Princípios SOLID Aplicados

O código foi cuidadosamente desenhado para evitar o forte acoplamento e o anti-pattern de "classes de domínio anêmicas". Abaixo estão os princípios aplicados:

1. SRP - Princípio da Responsabilidade Única (Single Responsibility Principle)

A classe Personagem não lida com a impressão de textos no console (System.out.println). Toda a parte de exibição de dados e formatação de idiomas foi separada nas interfaces OutputChannel e MessageProvider, sendo gerenciadas pelo mediador CombatNotifier.

2. OCP - Princípio Aberto-Fechado (Open/Closed Principle)

O sistema está aberto para extensão e fechado para modificação. Se quisermos adicionar um novo idioma (como Inglês ou Mandarim) ou uma nova saída de tela (como uma interface gráfica JavaFX), basta criar novas classes implementando MessageProvider ou OutputChannel, sem precisar alterar as regras de combate existentes.

3. LSP - Princípio da Substituição de Liskov (Liskov Substitution Principle)

Classes derivadas como Vilao, CavaleiroAntiMagia e CavaleiroMagico podem substituir a classe base Personagem em qualquer parte do código sem quebrar o funcionamento do programa. Todos cumprem os contratos de receber dano e verificar o status de vida corretamente.

4. ISP - Princípio da Segregação de Interface (Interface Segregation Principle)

Nem todo personagem tem habilidades mágicas ou especiais (como um inimigo comum ou um baú de cenário). Por isso, criamos a interface específica ComHabilidadeEspecial. Assim, não forçamos a classe Vilao a ter um método usarHabilidade() inútil.

5. DIP - Princípio da Inversão de Dependência (Dependency Inversion Principle)

O método atacar() não depende diretamente de outro Personagem, mas sim da abstração Destrutivel. Isso permite que, no futuro, possamos atacar objetos inanimados do cenário (como baús, portas ou muralhas) desde que eles implementem a interface Destrutivel.

🛠️ Tecnologias Utilizadas

Linguagem: Java (JDK 11+)

Paradigmas: Orientação a Objetos (POO)


Desenvolvido como estudo de Paradigmas da programação.
