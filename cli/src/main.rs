use echo::echo_service_client::EchoServiceClient;
use echo::EchoRequest;
use structopt::StructOpt;

/// echo client
#[derive(StructOpt, Debug)]
struct Opt {
    /// The text to send to the echo server
    #[structopt(name = "TEXT")]
    text: String,
}

pub mod echo {
    tonic::include_proto!("rocks.heikoseeberger.echo");
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let opt = Opt::from_args();

    let mut client = EchoServiceClient::connect("http://[::1]:8080").await?;

    let response = client.echo(EchoRequest { text: opt.text }).await?;
    println!("{}", response.get_ref().text);

    Ok(())
}
