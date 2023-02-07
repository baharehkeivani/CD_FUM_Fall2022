package compiler;

import gen.ToorlaLexer;
import gen.ToorlaListener;
import gen.ToorlaParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        CharStream stream = CharStreams.fromFileName("./sample/input.trl");
        ToorlaLexer lexer = new ToorlaLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ToorlaParser parser = new ToorlaParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();

        BufferedWriter out = new BufferedWriter(new FileWriter("output.txt", false));
        ToorlaListener listener = new CustomListener(out); //phase1
        ToorlaListener listenerWST = new CustomListenerWST(); //phase2-3
        walker.walk(listener, tree);
        walker.walk(listenerWST, tree);

        out.flush();
        out.close();
    }
}