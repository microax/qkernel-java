package com.qkernel.eobuilder;
/**
 * User: Nathan Oertel
 * Date: Jan 18, 2006
 * Time: 12:10:36 PM
 * Purpose: Generates EO, VO, and the Container files
 */
public class EOBuilder
{
    /**
     * This function generates the Container, EO, and VO.
     *
     * The arguments are as follows:
     *      First:  Daemon output directory
     *      Second: Entity Object output directory
     *      Third:  Database name
     *      Fourth: Container name
     *
     * @param args - arguments passed to the program
     */
    public static void main(String args[])
    {
        if(args.length!=4)
        {
            System.out.println("Usage: java EOBuilder <daemon-output-dir> <eo-output-dir> <database> <container-name>");
            System.exit(1);
        }
        else
        {
            try
            {
                /** build the container */
                ContainerBuilder cb=new ContainerBuilder
                    ("org.gjt.mm.mysql.Driver","jdbc:mysql://localhost:3316/"+args[2]+"?user=apache");
                cb.packageName="daemon";
                cb.containerName=args[3];
                cb.outputDir=args[0];

                cb.doBuild();

                /** build the entity objects */
                EntityObjectBuilder eob=new EntityObjectBuilder
                    ("org.gjt.mm.mysql.Driver","jdbc:mysql://localhost:3316/"+args[2]+"?user=apache");
                eob.packageName="entity";
                eob.useMorphers=false;
                eob.outputDir=args[1];

                eob.doBuild();

                /** build the value objects */
                ValueObjectBuilder vob=new ValueObjectBuilder
                    ("org.gjt.mm.mysql.Driver","jdbc:mysql://localhost:3316/"+args[2]+"?user=apache");
                vob.packageName="entity";
                vob.createMorphers=false;
                vob.outputDir=args[1];

                vob.doBuild();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
