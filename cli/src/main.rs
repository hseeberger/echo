use echo::echo_service_client::EchoServiceClient;
use echo::EchoRequest;
use echo::VersionRequest;
use echo::VersionResponse;
use structopt::StructOpt;

/// CLI for the echo service.
#[derive(StructOpt)]
struct Opt {
    #[structopt(subcommand)]
    command: Command,
}

#[derive(StructOpt)]
enum Command {
    /// Invokes the Echo method on the server
    Echo {
        /// The text to send to the echo server
        #[structopt(name = "TEXT")]
        text: String,
    },
    /// Invokes the Version method on the server
    Version,
}

pub mod echo {
    tonic::include_proto!("rocks.heikoseeberger.echo");
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut client = EchoServiceClient::connect("http://[::1]:8080").await?;

    let opt = Opt::from_args();

    match opt.command {
        Command::Echo { text } => {
            let response = client.echo(EchoRequest { text }).await?;
            println!("{}", response.get_ref().text);
        }
        Command::Version => {
            let response = client.version(VersionRequest {}).await?;
            let VersionResponse {
                version,
                scala_version,
            } = response.get_ref();
            println!("version: {}", version);
            println!("Scala version: {}", scala_version);
        }
    }

    Ok(())
}
