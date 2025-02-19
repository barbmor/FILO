package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import pl.opole.uni.cs.unifDL.Filo.controller.Solver;
import pl.opole.uni.cs.unifDL.Filo.model.Solution;

public class OntolgyTest {


	 boolean process(File input) {
		Solver solver = new Solver();
		if(solver.ini(input))
			solver.solve1();
		return solver.solved;
	}

	 void clearLog() {
		 try {
			new FileWriter("filoLog.txt").close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
		 
			@Test
			@DisplayName("FL0example2 -- success")
			void test2() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
				clearLog();
				Solution solution=null;
				OWLOntology ont=null;
				File input = new File("../Filo-unifier/tests/FL0example2.owx");
				Solver solver = new Solver();
				if(solver.ini(input)) {
					solver.solve1();
					solution=solver.getSolution();
					if(solution != null)
					 ont = solution.toOntology(solver.getAtomManager());
				}
				assertNotNull(ont);
				//fail("Not yet implemented");
			} 
		 
}
